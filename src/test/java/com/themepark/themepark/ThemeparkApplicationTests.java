package com.themepark.themepark;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import tools.jackson.databind.ObjectMapper;
import com.themepark.themepark.booking.BookingDtos.CreateRequest;
import com.themepark.themepark.booking.BookingDtos.LookupRequest;
import com.themepark.themepark.booking.PassType;
import com.themepark.themepark.resort.RoomType;
import com.themepark.themepark.resort.StayDtos;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ThemeparkApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void homepageAndAssetsArePublic() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Adventure World")))
				.andExpect(content().string(containsString("Four worlds.")))
				.andExpect(content().string(containsString("Make it a park day.")))
				.andExpect(content().string(containsString("_csrf")));

		mockMvc.perform(get("/tickets"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Your day out")));

		mockMvc.perform(get("/my-bookings"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Find your booking")));

		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Operator sign in")))
				.andExpect(content().string(containsString("name=\"username\"")));

		mockMvc.perform(get("/privacy-policy"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Information we collect")));

		mockMvc.perform(get("/terms-and-conditions"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Bookings and payment")));

		mockMvc.perform(get("/css/themepark.css"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("--pine")));

		mockMvc.perform(get("/js/adventure-world.js"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("/api/bookings")));

		mockMvc.perform(get("/api/experiences"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").exists());

		mockMvc.perform(get("/api/ticket-options"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].adultPrice").exists());
	}

	@Test
	void guestsCanCreateLookUpAndCancelBookings() throws Exception {
		CreateRequest request = new CreateRequest("Asha Rao", "asha@example.com", "+91 98765 43210",
				LocalDate.now().plusDays(30), 2, 2, PassType.DAY_PASS, 2, true);
		String created = mockMvc.perform(post("/api/bookings")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.total").value(5394.6))
				.andReturn().getResponse().getContentAsString();
		String reference = objectMapper.readTree(created).get("reference").asString();
		String lookup = objectMapper.writeValueAsString(new LookupRequest(reference, "asha@example.com"));

		mockMvc.perform(post("/api/bookings/lookup")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(lookup))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reference").value(reference));

		mockMvc.perform(post("/api/bookings/{reference}/cancel", reference)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON).content(lookup))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		mockMvc.perform(get("/admin").with(SecurityMockMvcRequestPostProcessors.user("test-operator").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString(reference)));
	}

	@Test
	void ticketReservationRequiresTermsAcceptance() throws Exception {
		CreateRequest request = new CreateRequest("Asha Rao", "asha@example.com", "",
				LocalDate.now().plusDays(30), 2, 0, PassType.DAY_PASS, 0, false);

		mockMvc.perform(post("/api/bookings")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void resortReservationRequiresTermsAcceptance() throws Exception {
		StayDtos.CreateRequest requestWithoutConsent = new StayDtos.CreateRequest(
				"Riya Shah", "riya@example.com", "", LocalDate.now().plusDays(45), 1, 2,
				RoomType.GARDEN_ROOM, false);

		mockMvc.perform(post("/api/stay-reservations")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestWithoutConsent)))
				.andExpect(status().isBadRequest());

		StayDtos.CreateRequest acceptedRequest = new StayDtos.CreateRequest(
				"Riya Shah", "riya@example.com", "", LocalDate.now().plusDays(45), 1, 2,
				RoomType.GARDEN_ROOM, true);
		mockMvc.perform(post("/api/stay-reservations")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(acceptedRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total").value(8999));
	}

	@Test
	void adminDashboardRequiresOperatorLogin() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/admin").with(SecurityMockMvcRequestPostProcessors.user("test-operator").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("ADVENTURE WORLD / OPERATIONS")));

		mockMvc.perform(formLogin("/login").user("username", "test-operator").password("test-password"))
				.andExpect(status().is3xxRedirection())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/admin"));
	}

}

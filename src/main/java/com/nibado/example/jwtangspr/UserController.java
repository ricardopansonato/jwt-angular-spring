package com.nibado.example.jwtangspr;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@PostConstruct
	public void init() {
		UserInformation user1 = new UserInformation();
		user1.profiles = new ArrayList<String> () {{ add("user"); }};
		user1.name = "tom";
		stringRedisTemplate.opsForHash().putAll("User:" + user1.name, user1.toMap());

		UserInformation user2 = new UserInformation();
		user2.profiles = new ArrayList<String> () {{ add("admin"); }};
		user2.name = "sally";
		stringRedisTemplate.opsForHash().putAll("User:" + user2.name, user2.toMap());
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public void sendUserInformation(@RequestBody final UserInformation user, HttpServletResponse response) throws ServletException {

		if (user.name == null) {
			throw new ServletException("Invalid login");
		}

		UserInformation user1 = UserInformation.toObject(stringRedisTemplate.opsForHash().entries("User:" + user.name));

		if (user1 == null) {
			throw new ServletException("Invalid login");
		}

		final String jwt = Jwts.builder().setSubject(user.name).claim("roles", user1.getProfiles()).setIssuedAt(new Date())
				.signWith(SignatureAlgorithm.HS256, "secretkey").compact();
		stringRedisTemplate.opsForValue().set("Token:" + user1.name, jwt, 24, TimeUnit.HOURS);
		Cookie cookie = new Cookie("X-AUTH-TOKEN", jwt);
		cookie.setMaxAge(100000);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	private static class UserInformation implements Serializable {

		private static final long serialVersionUID = 441251932471408122L;

		private String name;
		private List<String> profiles;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getProfiles() {
			return profiles;
		}

		public void setProfiles(List<String> profiles) {
			this.profiles = profiles;
		}

		public static UserInformation toObject(Map<Object, Object> map) {
			UserInformation user = new UserInformation();
			user.name = (String) map.get("name");
			user.profiles = Arrays.asList(map.get("profiles").toString().split(","));
			return user;
		}

		public Map<String, Object> toMap() {
			final Map<String, Object> properties = new HashMap<String, Object>();

			if (name != null) {
				properties.put("name", name);
			}

			if (profiles != null) {
				properties.put("profiles", String.join(",", profiles));
			}

			return properties;
		}
	}

	private static class Token {
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public Token(final String token) {
			this.token = token;
		}
	}
}

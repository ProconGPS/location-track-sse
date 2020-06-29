package com.spring.see.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

@SpringBootApplication
@EnableWebFlux
public class SSEServerApplication {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RestController
    @RequestMapping("/sse-server")
    class LiveLocationController {

        @Autowired
        LocationTrackService locationTrackService;

        @CrossOrigin
        @GetMapping(path = "/livetracking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<LocationDto> liveTracking(@RequestParam("lng") double lng, @RequestParam("lat") double lat, @RequestParam("radius") double radius) {
            LocationDto locationDto = new LocationDto();
            locationDto.setLng(lng);
            locationDto.setLat(lat);
            return Flux.interval(Duration.ofSeconds(1))
                    .map(sequence -> locationTrackService.getLocation(radius,locationDto));
        }
    }

    @Service
    class LocationTrackService {
        public LocationDto getLocation(double radius, LocationDto locationDto) {
            double x0 = locationDto.getLng();
            double y0 = locationDto.getLat();
            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(Math.toRadians(y0));

            double foundLongitude = new_x + x0;
            double foundLatitude = y + y0;
            logger.info("Longitude: " + foundLongitude + "  Latitude: " + foundLatitude );

            locationDto.setLat(foundLatitude);
            locationDto.setLng(foundLongitude);
            return locationDto;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SSEServerApplication.class, args);
    }

}

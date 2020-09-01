package com.space.service;

import com.space.model.Ship;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

public class ShipService {
    private final static int CURRENT_YEAR = 3019;

    public static void setRating(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        ship.setRating(new BigDecimal(80 * ship.getSpeed() * (ship.getUsed() ? 0.5 : 1.0) /
                (CURRENT_YEAR - calendar.get(Calendar.YEAR) + 1))
                .setScale(2, RoundingMode.HALF_EVEN).doubleValue());
    }
}

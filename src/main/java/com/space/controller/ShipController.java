package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {
    @Autowired
    private ShipRepository shipRepository;

    @GetMapping("/ships")
    public List<Ship> getShips(@RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "planet", required = false) String planet,
                               @RequestParam(value = "shipType", required = false) ShipType shipType,
                               @RequestParam(value = "after", required = false) Long after,
                               @RequestParam(value = "before", required = false) Long before,
                               @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                               @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                               @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                               @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                               @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                               @RequestParam(value = "minRating", required = false) Double minRating,
                               @RequestParam(value = "maxRating", required = false) Double maxRating,
                               @RequestParam(value = "order", required = false) ShipOrder order,
                               @RequestParam(value = "pageNumber", required = false, defaultValue = "0")
                                       Integer pageNumber,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "3")
                                       Integer pageSize) {
        List<Ship> result = new ArrayList<>();
        shipRepository.find(PageRequest.of(pageNumber, pageSize,
                order == null ? Sort.unsorted() : Sort.by(order.getFieldName())),
                name,
                planet,
                shipType,
                after == null ? null : new Date(after),
                before == null ? null : new Date(before),
                isUsed,
                minSpeed,
                maxSpeed,
                minCrewSize,
                maxCrewSize,
                minRating,
                maxRating)
                .forEach(result::add);
        return result;
    }

    @GetMapping("/ships/count")
    public Integer getCount(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "planet", required = false) String planet,
                            @RequestParam(value = "shipType", required = false) ShipType shipType,
                            @RequestParam(value = "after", required = false) Long after,
                            @RequestParam(value = "before", required = false) Long before,
                            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                            @RequestParam(value = "minRating", required = false) Double minRating,
                            @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return shipRepository.getCount(
                name,
                planet,
                shipType,
                after == null ? null : new Date(after),
                before == null ? null : new Date(before),
                isUsed,
                minSpeed,
                maxSpeed,
                minCrewSize,
                maxCrewSize,
                minRating,
                maxRating
        );
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable Long id) {
        if (id == null || id <= 0) return ResponseEntity.badRequest().build();
        Ship ship = shipRepository.findById(id).orElse(null);
        if (ship == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ship);
    }

    @PostMapping("/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        Calendar calendar = Calendar.getInstance();
        if (ship.getProdDate() != null) calendar.setTime(ship.getProdDate());

        if (ship.getName() == null ||
                ship.getPlanet() == null ||
                ship.getShipType() == null ||
                ship.getProdDate() == null ||
                ship.getSpeed() == null ||
                ship.getCrewSize() == null ||
                ship.getName().length() > 50 ||
                ship.getPlanet().length() > 50 ||
                ship.getName().isEmpty() ||
                ship.getPlanet().isEmpty() ||
                ship.getSpeed() < 0.01 ||
                ship.getSpeed() > 0.99 ||
                ship.getCrewSize() < 1 ||
                ship.getCrewSize() > 9999 ||
                ship.getProdDate().getTime() < 0 ||
                calendar.get(Calendar.YEAR) < 2800 ||
                calendar.get(Calendar.YEAR) > 3019)
            return ResponseEntity.badRequest().build();
        if (ship.getUsed() == null) ship.setUsed(false);
        ship.setId(null);
        ShipService.setRating(ship);
        ship.setSpeed(
                new BigDecimal(ship.getSpeed()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        return ResponseEntity.ok(shipRepository.save(ship));
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable Long id, @RequestBody Ship ship) {
        Calendar calendar = Calendar.getInstance();
        if (ship.getProdDate() != null) calendar.setTime(ship.getProdDate());

        if (id == null ||
                id <= 0 ||
                (ship.getName() != null && (ship.getName().length() > 50 || ship.getName().isEmpty())) ||
                (ship.getCrewSize() != null && (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999)) ||
                (ship.getProdDate() != null && (
                        calendar.get(Calendar.YEAR) < 2800 ||
                                calendar.get(Calendar.YEAR) > 3019)))
            return ResponseEntity.badRequest().build();
        Ship oldShip = shipRepository.findById(id).orElse(null);
        if (oldShip == null) return ResponseEntity.notFound().build();
        if (ship.getName() != null) oldShip.setName(ship.getName());
        if (ship.getPlanet() != null) oldShip.setPlanet(ship.getPlanet());
        if (ship.getShipType() != null) oldShip.setShipType(ship.getShipType());
        if (ship.getProdDate() != null) oldShip.setProdDate(ship.getProdDate());
        if (ship.getUsed() != null) oldShip.setUsed(ship.getUsed());
        if (ship.getSpeed() != null) oldShip.setSpeed(
                new BigDecimal(ship.getSpeed()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        if (ship.getCrewSize() != null) oldShip.setCrewSize(ship.getCrewSize());
        ShipService.setRating(oldShip);
        return ResponseEntity.ok(shipRepository.save(oldShip));
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity deleteShip(@PathVariable Long id) {
        if (id == null || id <= 0) return ResponseEntity.badRequest().build();
        Ship oldShip = shipRepository.findById(id).orElse(null);
        if (oldShip == null) return ResponseEntity.notFound().build();
        shipRepository.delete(oldShip);
        return ResponseEntity.ok().build();
    }
}

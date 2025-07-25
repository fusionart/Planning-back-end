package com.monbat.planning.controllers;

import com.monbat.planning.services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/api/sap")
public class MaterialController implements Serializable {
    @Autowired
    private MaterialService materialService;

    @RequestMapping(value = "/getMaterials", method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public List<?> getMaterials() {
        return this.materialService.getAllMaterials();
    }
}

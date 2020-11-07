package com.xp.id.idgenerator.controller;

import com.xp.id.idgenerator.service.IDGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class GeneratorController {
    Logger logger = LoggerFactory.getLogger(GeneratorController.class);

    @Autowired
    IDGeneratorService idGeneratorService;

    @RequestMapping("/generate")
    public Long generator(@RequestParam String name) {
        long id = idGeneratorService.generator(name);
        logger.info(id + "");
        return id;
    }
}

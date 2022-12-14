package cz.los.alice.controller;

import cz.los.alice.service.ProcessingResult;
import cz.los.alice.service.AliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AliceController {

    static final String HELLO = "Hi. To start processing follow <a href=\"/process\">THIS</a> link";

    private final AliceService service;

    @RequestMapping("/")
    public String index() {
        return HELLO;
    }

    @GetMapping("/process")
    public ProcessingResult process() {
        return service.process();
    }
}

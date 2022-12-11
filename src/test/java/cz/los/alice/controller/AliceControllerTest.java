package cz.los.alice.controller;

import cz.los.alice.service.AliceService;
import cz.los.alice.service.ProcessingResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AliceControllerTest {

    private AliceService service;
    private AliceController controller;

    @BeforeEach
    private void setup() {
        this.service = Mockito.mock(AliceService.class);
        this.controller = Mockito.spy(new AliceController(service));
    }

    @Test
    @DisplayName("Should return expected welcome message with a link on '/index' call")
    public void indexTest() {
        String result = controller.index();
        verify(controller, times(1)).index();
        Assertions.assertEquals(AliceController.HELLO, result);
    }

    @Test
    @DisplayName("Should return expected processing result on '/process' call")
    public void processTest() {
        ProcessingResult expected = new ProcessingResult(42, 42, 42,
                Collections.emptyList(), Collections.emptyList());
        when(service.process()).thenReturn(expected);
        ProcessingResult result = controller.process();
        verify(service, times(1)).process();
        Assertions.assertEquals(expected, result);
    }

}

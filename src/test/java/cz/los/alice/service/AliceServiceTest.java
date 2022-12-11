package cz.los.alice.service;

import cz.los.alice.cpm.CpmGraph;
import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.cpm.CpmProcessorFactory;
import cz.los.alice.cpm.Node;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AliceServiceTest {

    private TaskParser parser;
    private CpmProcessorFactory factory;
    private CpmProcessor processor;
    private CpmGraph cpmGraph;
    private AliceService service;

    @BeforeEach
    public void setup() {
        this.parser = Mockito.mock(TaskParser.class);
        this.factory = Mockito.mock(CpmProcessorFactory.class);
        this.processor = Mockito.mock(CpmProcessor.class);
        this.cpmGraph = Mockito.mock(CpmGraph.class);
        this.service = new AliceService(parser, factory);

        Set<Task> tasks = Collections.emptySet();

        when(parser.parseInputFile()).thenReturn(tasks);
        when(factory.createCpmProcessor(tasks)).thenReturn(processor);
        when(processor.buildCpmGraph()).thenReturn(cpmGraph);
        when(processor.buildCriticalPath(any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Should return processing result. Happy Test")
    public void processHappyTest() {

        when(processor.createWorkersOnSiteStatistics(any())).thenReturn(Map.of(69,420));
        when(cpmGraph.getEndNode())
                .thenReturn(new Node(Task.builder().taskCode("Test").dependencies(Collections.emptyList()).build()));

        ProcessingResult result = service.process();

        verify(parser, times(1)).parseInputFile();
        verify(factory, times(1)).createCpmProcessor(any());
        verify(processor, times(1)).buildCpmGraph();
        verify(processor, times(1)).buildCriticalPath(cpmGraph);
        verify(processor, times(1)).createWorkersOnSiteStatistics(cpmGraph);

        assertNotNull(result);
        assertEquals(69, result.getMostBusyDay());
        assertEquals(420, result.getMaxWorkersOnSite());
    }

    @Test
    @DisplayName("Should throw a RuntimeException if most busy day map has no data")
    public void noDataInBusyDayMap() {
        when(processor.createWorkersOnSiteStatistics(any())).thenReturn(Collections.emptyMap());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> service.process());

        verify(parser, times(1)).parseInputFile();
        verify(factory, times(1)).createCpmProcessor(any());
        verify(processor, times(1)).buildCpmGraph();
        verify(processor, times(1)).buildCriticalPath(cpmGraph);
        verify(processor, times(1)).createWorkersOnSiteStatistics(cpmGraph);

        assertEquals("Could not find max value in provided list", thrown.getMessage());
    }
}

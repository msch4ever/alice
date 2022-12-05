package cz.los.alice.cmp;

import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class CpmProcessorTest {

    @Test
    public void test() {
        List<Task> tasks = List.of(
                Task.builder()
                        .taskCode("A")
                        .duration(3)
                        .dependencies(Collections.emptyList())
                        .build(),
                Task.builder()
                        .taskCode("B")
                        .duration(6)
                        .dependencies(Collections.emptyList())
                        .build(),
                Task.builder()
                        .taskCode("C")
                        .duration(4)
                        .dependencies(List.of("B"))
                        .build(),
                Task.builder()
                        .taskCode("D")
                        .duration(2)
                        .dependencies(List.of("A"))
                        .build(),
                Task.builder()
                        .taskCode("E")
                        .duration(4)
                        .dependencies(List.of("C", "D"))
                        .build(),
                Task.builder()
                        .taskCode("F")
                        .duration(3)
                        .dependencies(List.of("E"))
                        .build(),
                Task.builder()
                        .taskCode("G")
                        .duration(7)
                        .dependencies(List.of("D"))
                        .build()
        );
        CpmProcessor cpm = new CpmProcessor(tasks);
        cpm.applyCpm();
        assert true;
    }

}
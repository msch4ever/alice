package cz.los.alice.cmp;

import cz.los.alice.cpm.CpmProcessor;
import cz.los.alice.model.Crew;
import cz.los.alice.model.Task;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CpmProcessorTest {

    @Test
    @Disabled("original dev runner method")
    public void test() {
        Set<Task> tasks = Set.of(
                Task.builder()
                        .taskCode("A")
                        .duration(3)
                        .crew(Crew.builder()
                                .assignment(2)
                                .build())
                        .dependencies(Collections.emptyList())
                        .build(),
                Task.builder()
                        .taskCode("B")
                        .duration(6)
                        .crew(Crew.builder()
                                .assignment(1)
                                .build())
                        .dependencies(Collections.emptyList())
                        .build(),
                Task.builder()
                        .taskCode("C")
                        .duration(4)
                        .crew(Crew.builder()
                                .assignment(2)
                                .build())
                        .dependencies(List.of("B"))
                        .build(),
                Task.builder()
                        .taskCode("D")
                        .duration(2)
                        .crew(Crew.builder()
                                .assignment(3)
                                .build())
                        .dependencies(List.of("A"))
                        .build(),
                Task.builder()
                        .taskCode("E")
                        .duration(4)
                        .crew(Crew.builder()
                                .assignment(4)
                                .build())
                        .dependencies(List.of("C", "D"))
                        .build(),
                Task.builder()
                        .taskCode("F")
                        .duration(3)
                        .crew(Crew.builder()
                                .assignment(1)
                                .build())
                        .dependencies(List.of("E"))
                        .build(),
                Task.builder()
                        .taskCode("G")
                        .duration(7)
                        .crew(Crew.builder()
                                .assignment(2)
                                .build())
                        .dependencies(List.of("D"))
                        .build()
        );
        CpmProcessor cpm = new CpmProcessor(tasks);
        cpm.buildCpmGraph();
        assert true;
    }

}

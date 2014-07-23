import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.protocolanalyzer.api.LogicBitSet;
import org.junit.Test;

import java.util.Random;

public class ClonnableTest extends AbstractBenchmark{

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void testCloneLogicBitSet(){
        LogicBitSet logicBitSet = new LogicBitSet();
        Random random = new Random();

        // Add some data
        for(int n = 0; n < 20; ++n){
            logicBitSet.set(n, random.nextBoolean());
        }

        LogicBitSet clonedBitSet = new LogicBitSet(logicBitSet);
        System.out.println("Cloned BitSet before clear: " + clonedBitSet);
        System.out.println("BitSet before clear:        " + logicBitSet);

        System.out.println("Cloned BitSet size: " + clonedBitSet.length());
        System.out.println("BitSet size:        " + logicBitSet.length() + "\n\n");

        logicBitSet.clear();
        System.out.println("Cloned BitSet after clear:  " + clonedBitSet);
        System.out.println("BitSet after clear:         " + logicBitSet);

        System.out.println("Cloned BitSet size: " + clonedBitSet.length());
        System.out.println("BitSet size:        " + logicBitSet.length());

    }

}

package metamorphicRelationsInference.util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import metamorphicRelationsInference.metamorphicRelation.GenerationStrategy;

@Parameters(separators = "=")
public class AdditionalOptions {

    @Parameter(names = "--gen-strategy")
    private String strategy;

    @Parameter(names = "--mrs-to-fuzz")
    private int mrsToFuzz = 0;

    @Parameter(names = "--allow-epa-loops", arity = 1)
    private boolean allowEPALoops = false;

    public GenerationStrategy generationStrategy() {
        return GenerationStrategy.valueOf(strategy);
    }

    public int mrsToFuzz() {
        return mrsToFuzz;
    }

    public boolean isEPALoopsAllowed() {
        return allowEPALoops;
    }

}

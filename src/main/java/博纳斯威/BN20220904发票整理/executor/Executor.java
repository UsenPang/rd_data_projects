package 博纳斯威.BN20220904发票整理.executor;

import lombok.Data;

import java.util.Collection;

@Data
public class Executor {
    private Collection<String> hitCollection;
    private String source;
}

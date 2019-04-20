package br.ufsc.ppgcc.experion.extractor.input.engine.lattes;

import br.ufsc.ppgcc.experion.extractor.input.engine.technique.KeygraphExtractionTechniqueTFIDF;

public class KeygraphExtractionTechniqueLattesTFIDF extends KeygraphExtractionTechniqueTFIDF {

    public KeygraphExtractionTechniqueLattesTFIDF() {
        super(new KeyGraphLattes());
    }

}

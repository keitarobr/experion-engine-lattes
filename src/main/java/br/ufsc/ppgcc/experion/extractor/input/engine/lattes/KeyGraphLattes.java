package br.ufsc.ppgcc.experion.extractor.input.engine.lattes;

import br.ufsc.ppgcc.experion.extractor.algorithm.keygraph.Keygraph;

import java.io.InputStream;
import java.net.URL;

public class KeyGraphLattes extends Keygraph {
    @Override
    public InputStream getConfig() {
        return this.getClass().getResourceAsStream("/LattesConstants.txt");
    }
}

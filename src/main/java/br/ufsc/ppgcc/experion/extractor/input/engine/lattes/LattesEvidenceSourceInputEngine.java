package br.ufsc.ppgcc.experion.extractor.input.engine.lattes;

import br.ufsc.ppgcc.experion.Experion;
import br.ufsc.ppgcc.experion.extractor.evidence.PhysicalEvidence;
import br.ufsc.ppgcc.experion.extractor.input.EvidenceSourceInput;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.ExtractionTechnique;
import br.ufsc.ppgcc.experion.extractor.input.BaseSourceInputEngine;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.LDAExtractionTechnique;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.LDAExtractionTechniqueTFIDF;
import br.ufsc.ppgcc.experion.extractor.input.engine.technique.TFIDFExtractionTechnique;
import br.ufsc.ppgcc.experion.model.expert.Expert;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.*;
import java.util.*;

public class LattesEvidenceSourceInputEngine extends BaseSourceInputEngine implements Serializable {

    private transient Connection connection;


    public static class LDA extends LattesEvidenceSourceInputEngine {
        public LDA() throws SQLException, ClassNotFoundException {
            super(new LDAExtractionTechnique(), true);
        }
    }

    public static class LDATFIDF extends LattesEvidenceSourceInputEngine {
        public LDATFIDF() throws SQLException, ClassNotFoundException {
            super(new LDAExtractionTechniqueTFIDF(), true);
        }
    }

    public static class TFIDF extends LattesEvidenceSourceInputEngine {
        public TFIDF() throws SQLException, ClassNotFoundException {
            super(new TFIDFExtractionTechnique(), true);
        }
    }

    public static class KeyGraph extends LattesEvidenceSourceInputEngine {
        public KeyGraph() throws SQLException, ClassNotFoundException {
            super(new KeygraphExtractionTechniqueLattes(), true);
        }
    }

    public static class KeyGraphTFIDF extends LattesEvidenceSourceInputEngine {
        public KeyGraphTFIDF() throws SQLException, ClassNotFoundException {
            super(new KeygraphExtractionTechniqueLattesTFIDF(), true);
        }
    }


    public LattesEvidenceSourceInputEngine() throws SQLException, ClassNotFoundException {
        this(null, false);
    }

    public LattesEvidenceSourceInputEngine(ExtractionTechnique extractionTechnique, boolean connectToDatabase) {
        setExtractionTechnique(extractionTechnique);
        if (connectToDatabase) {
            try {
                this.connectToDatabase();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public LattesEvidenceSourceInputEngine(ExtractionTechnique extractionTechnique) {
        this(extractionTechnique, false);
    }

    public void connectToDatabase() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            String url = "jdbc:postgresql://%s:%d/%s";
            Class.forName("org.postgresql.Driver");
            url = url.format(url, Experion.getInstance().getConfig().getString("lattes.db.host"), Experion.getInstance().getConfig().getInt("lattes.db.port"),
                    Experion.getInstance().getConfig().getString("lattes.db.database"));
            connection = DriverManager.getConnection(url, Experion.getInstance().getConfig().getString("lattes.db.user"), Experion.getInstance().getConfig().getString("lattes.db.password"));
        }
    }

    public void disconnectDatabase() throws SQLException {
        connection.close();
    }

    @Override
    public Set<Expert> getExpertEntities() {
        try {
            this.connectToDatabase();
            Set<Expert> entities = new HashSet<>();
            Statement st = null;
            st = connection.createStatement();
            ResultSet rs = st.executeQuery("select distinct lattes_id,nome_completo from vw_producao order by lattes_id asc");
            while (rs.next()) {
                Expert expert = new Expert(rs.getString(1), rs.getString(2));
                expert.setIdentification(rs.getString(1));
                expert.setName(rs.getString(2));
                entities.add(expert);
            }
            rs.close();
            return entities;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<PhysicalEvidence> getNewEvidences(Expert expert, EvidenceSourceInput input) {
        String idInSource = expert.getIdentificationForSource(this.getEvidenceSource());
        try {
            this.connectToDatabase();

            PreparedStatement st = null;
            Set<PhysicalEvidence> evidences = new HashSet<>();
            try {
                st = connection.prepareStatement("select ano, titulo, descricao from vw_producao where lattes_id=? order by lattes_id,ano asc");
                st.setString(1, idInSource);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    PhysicalEvidence evidence = new PhysicalEvidence();
                    evidence.setExpert(expert);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.MONTH, 0);
                    cal.set(Calendar.YEAR, rs.getInt(1));
                    evidence.setTimestamp(cal.getTime());

                    String keywords = "";
                    if (!StringUtils.isBlank(rs.getString(2))) {
                        keywords += rs.getString(2);
                    }
                    if (!StringUtils.isBlank(rs.getString(3))) {
                        keywords += " " + rs.getString(3);
                    }
                    keywords = keywords.trim();
                    if (!StringUtils.isBlank(keywords)) {
                        evidence.addKeywords(keywords.split(" "));
                    }

                    EvidenceSourceURLLattes url = new EvidenceSourceURLLattes();
                    evidence.setInput(input);
                    url.setUrl("Currículo lattes");
                    url.setRetrievedData(String.format("Ano: %s\nTítulo: %s %s", rs.getString(1), rs.getString(2), rs.getString(3) != null ? String.format("\nDescrição:", rs.getString(3)) : "" ));
                    evidence.setUrl(url);
                    evidences.add(evidence);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            evidences = customizeEvidences(expert, evidences);

            return evidences;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Set<PhysicalEvidence> customizeEvidences(Expert expert, Set<PhysicalEvidence> evidences) {
        return this.getExtractionTechnique().generateEvidences(expert, evidences, this.getLanguage());
    }

    @Override
    public Set<Expert> findExpertByName(String name) {
        try {
            this.connectToDatabase();
            Set<Expert> entities = new HashSet<>();
            PreparedStatement st = connection.prepareStatement("select distinct lattes_id,nome_completo from vw_producao where lower(nome_completo) like ? order by lattes_id asc");
            st.setString(1, "%" + name.toLowerCase() + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Expert expert = new Expert(rs.getString(1), rs.getString(2));
                expert.setIdentification(rs.getString(1));
                expert.setName(rs.getString(2));
                entities.add(expert);
            }
            rs.close();
            return entities;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

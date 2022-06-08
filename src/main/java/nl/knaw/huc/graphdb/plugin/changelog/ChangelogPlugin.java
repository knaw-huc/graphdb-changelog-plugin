package nl.knaw.huc.graphdb.plugin.changelog;

import com.ontotext.trree.sdk.*;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.eclipse.rdf4j.model.util.Statements.statement;

public class ChangelogPlugin extends PluginBase implements StatementListener {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Instant curInstant;
    private OutputStream curOutputStream;
    private NQuadsUdWriter curRdfWriter;

    @Override
    public String getName() {
        return "changelog-plugin";
    }

    @Override
    public boolean statementAdded(long subject, long predicate, long object, long context, boolean explicit,
                                  PluginConnection pluginConnection) {
        if (explicit) {
            logStatement(subject, predicate, object, context, true, pluginConnection);
        }

        return false;
    }

    @Override
    public boolean statementRemoved(long subject, long predicate, long object, long context, boolean explicit,
                                    PluginConnection pluginConnection) {
        if (explicit) {
            logStatement(subject, predicate, object, context, false, pluginConnection);
        }

        return false;
    }

    private void logStatement(long subject, long predicate, long object, long context, boolean isAssertion,
                              PluginConnection pluginConnection) {
        try {
            setupWriter();
            Statement statement = statement(
                    (Resource) pluginConnection.getEntities().get(subject),
                    (IRI) pluginConnection.getEntities().get(predicate),
                    pluginConnection.getEntities().get(object),
                    (context != 0) ? (Resource) pluginConnection.getEntities().get(context) : null);
            curRdfWriter.handleStatement(isAssertion, statement);
        } catch (IOException e) {
            getLogger().error("Error in changelog plugin: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setupWriter() throws IOException {
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (curInstant == null || !curInstant.equals(today)) {
            if (curRdfWriter != null) {
                curRdfWriter.endRDF();
                curOutputStream.close();
            }

            Path logFile = getDataDir().toPath().resolve(DATE_FORMAT.format(new Date()) + ".nqud");
            if (!Files.exists(getDataDir().toPath())) {
                getLogger().info("Creating log directory: " + getDataDir().toPath().toAbsolutePath());
                Files.createDirectory(getDataDir().toPath());
            }
            if (!Files.exists(logFile)) {
                getLogger().info("Creating log file: " + logFile.toAbsolutePath());
                Files.createFile(logFile);
            }

            curInstant = today;
            curOutputStream = Files.newOutputStream(logFile);
            curRdfWriter = new NQuadsUdWriter(curOutputStream);
            curRdfWriter.startRDF();
        }
    }
}
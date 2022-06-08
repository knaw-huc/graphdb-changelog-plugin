package nl.knaw.huc.graphdb.plugin.changelog;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.rdf4j.rio.RDFFormat.*;

public class NQuadsUdWriter extends NQuadsWriter implements RDFAssertionHandler {
    protected BiConsumer<Boolean, Statement> statementConsumer;

    public NQuadsUdWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public NQuadsUdWriter(Writer writer) {
        super(writer);
    }

    @Override
    public RDFFormat getRDFFormat() {
        return new RDFFormat(
                "NQuadsUnifiedDiff",
                "application/vnd.timbuctoo-rdf.nquads_unified_diff",
                UTF_8,
                "nqud",
                NO_NAMESPACES,
                SUPPORTS_CONTEXTS,
                NO_RDF_STAR
        );
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        super.startRDF();

        statementConsumer = this::consumeStatement;
        // TODO: Convert RDF-star
        // if (getWriterConfig().get(BasicWriterSettings.CONVERT_RDF_STAR_TO_REIFICATION)) {
        //     // All writers can convert RDF-star to reification on request
        //     statementConsumer = this::handleStatementConvertRDFStar;
        // } else if (!getRDFFormat().supportsRDFStar() && getWriterConfig().get(BasicWriterSettings.ENCODE_RDF_STAR)) {
        //     // By default non-RDF-star writers encode RDF-star to special RDF IRIs
        //     // (all parsers, including RDF-star will convert back the encoded IRIs)
        //     statementConsumer = this::handleStatementEncodeRDFStar;
        // }
    }

    @Override
    public void handleStatement(boolean isAssertion, Statement st) throws RDFHandlerException {
        checkWritingStarted();
        statementConsumer.accept(isAssertion, st);
    }

    public void consumeStatement(boolean isAssertion, Statement st) throws RDFHandlerException {
        try {
            writer.write(isAssertion ? "+" : "-");
            consumeStatement(st);
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }
}

package nl.knaw.huc.graphdb.plugin.changelog;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;

public interface RDFAssertionHandler {
    void handleStatement(boolean isAssertion, Statement statement) throws RDFHandlerException;
}

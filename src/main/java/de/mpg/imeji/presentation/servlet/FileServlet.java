/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.opensaml.ws.security.provider.HTTPRule;

import de.mpg.imeji.logic.search.Search;
import de.mpg.imeji.logic.search.Search.SearchType;
import de.mpg.imeji.logic.search.query.SPARQLQueries;
import de.mpg.imeji.logic.security.Operations.OperationsType;
import de.mpg.imeji.logic.security.Security;
import de.mpg.imeji.logic.storage.Storage;
import de.mpg.imeji.logic.storage.StorageController;
import de.mpg.imeji.logic.storage.util.StorageUtils;
import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.util.StringHelper;
import de.mpg.imeji.logic.vo.CollectionImeji;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.presentation.beans.Navigation;
import de.mpg.imeji.presentation.session.SessionBean;
import de.mpg.imeji.presentation.util.ObjectLoader;

/**
 * The Servlet to Read files from imeji {@link Storage}
 * 
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public class FileServlet extends HttpServlet
{
    private static final long serialVersionUID = 5502546330318540997L;
    private static Logger logger = Logger.getLogger(FileServlet.class);
    private StorageController storageController;
    private Security security;
    private Navigation navivation;
    private String domain;

    @Override
    public void init()
    {
        try
        {
            storageController = new StorageController();
            logger.info("ImageServlet initialized");
            security = new Security();
            navivation = new Navigation();
            domain = StringHelper.normalizeURI(navivation.getDomain());
            domain = domain.substring(0, domain.length() - 1);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Image servlet not initialized! " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String url = req.getParameter("id");
        if (req.getRequestedSessionId() != null)
        {
            if (url == null)
            {
                // if the id parameter is null, interpret the whole url as a direct to the file (can only work if the
                // internal storage is used)
                url = domain + req.getRequestURI();
            }
            resp.setContentType(StorageUtils.getMimeType(StringHelper.getFileExtension(url)));
            SessionBean session = getSession(req);
            if (security.check(OperationsType.READ, getUser(session), loadCollection(url, session)))
            {
                storageController.read(url, resp.getOutputStream(), false);
            }
            else
            {
                resp.sendError(403, "imeji security: You are not allowed to view this file");
            }
        }
        else
        {
            // Avoid to reset the session. If the session in request has no id, this means there has been a problem
            // (because of some redirect?). To avoid the session to be killed, this make a redirect to the correct page,
            // where the session should be non null
            resp.sendRedirect(req.getRequestURL().toString() + "?id=" + url);
        }
    }

    /**
     * Load a {@link CollectionImeji} from the session if possible, otherwise from jena
     * 
     * @param uri
     * @param user
     * @return
     */
    private CollectionImeji loadCollection(String url, SessionBean session)
    {
        URI collectionURI = getCollectionURI(url);
        if (collectionURI == null)
            return null;
        CollectionImeji collection = session.getCollectionCached().get(collectionURI);
        if (collection == null)
        {
            try
            {
                // important to use lazy load, otherwise high performance issue
                collection = ObjectLoader.loadCollectionLazy(collectionURI, session.getUser());
                session.getCollectionCached().put(collection.getId(), collection);
            }
            catch (Exception e)
            {
                /* user is not allowed to view this collection */
            }
        }
        return collection;
    }

    /**
     * Return the uri of the {@link CollectionImeji} of the file with this url
     * 
     * @param url
     * @return
     */
    private URI getCollectionURI(String url)
    {
        String id = storageController.getCollectionId(url);
        if (id != null)
        {
            return ObjectHelper.getURI(CollectionImeji.class, id);
        }
        else
        {
            Search s = new Search(SearchType.ALL, null);
            List<String> r = s.searchSimpleForQuery(SPARQLQueries.selectCollectionIdOfFile(url), null);
            if (!r.isEmpty())
                return URI.create(r.get(0));
            else
                return null;
        }
    }

    /**
     * Read the user in the session
     * 
     * @param req
     * @return
     */
    private User getUser(SessionBean sessionBean)
    {
        if (sessionBean != null)
        {
            return sessionBean.getUser();
        }
        return null;
    }

    /**
     * Return the {@link SessionBean} form the {@link HttpSession}
     * 
     * @param req
     * @return
     */
    private SessionBean getSession(HttpServletRequest req)
    {
        return (SessionBean)req.getSession().getAttribute(SessionBean.class.getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // No post action
        return;
    }
}

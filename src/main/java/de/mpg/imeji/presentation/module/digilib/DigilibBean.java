package de.mpg.imeji.presentation.module.digilib;

import java.io.File;
import java.net.URL;

import javax.xml.ws.Holder;

import org.apache.log4j.Logger;

import de.mpg.imeji.logic.digilib.Scaler;
import de.mpg.imeji.logic.vo.CollectionImeji;
import de.mpg.imeji.presentation.session.SessionBean;
import de.mpg.imeji.presentation.util.BeanHelper;

/**
 * Bean for a Single image in digilib
 * 
 * @author hnguyen (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public class DigilibBean {

	private SessionBean session = null;
    private String collectionId;
    private CollectionImeji collection;
    private static Logger logger = Logger.getLogger(DigilibBean.class);
    private boolean error = false;
    private boolean success = false;
    private String msg = "";
    private File file = null;
    private Scaler digilibScaler = null;


	/**
     * Default constructor
     */
    public DigilibBean()
    {
        this.session = (SessionBean)BeanHelper.getSessionBean(SessionBean.class);
        this.digilibScaler = new Scaler();
    }
    
    public byte[] getScaledImage(String sid, String uri, String query, URL serverUrl) {
        // Holder for return data
        Holder<String> mimeType = new Holder<String>();
        Holder<byte[]> imageData = new Holder<byte[]>();
        Holder<Integer> width = new Holder<Integer>();
        Holder<Integer> height = new Holder<Integer>();
        Holder<Integer> originalWidth = new Holder<Integer>();
        Holder<Integer> originalHeight = new Holder<Integer>();
        Holder<Integer> originalDpi = new Holder<Integer>();
        // call service -- fills Holder
        this.digilibScaler.getScaledImage(sid, uri, query, serverUrl.toString(), mimeType, imageData, width, height, originalWidth,
                originalHeight, originalDpi);
        // print values
//        System.out.println("mimeType=" + mimeType.value);
//        System.out.println("imageData=" + imageData.value);
//        System.out.println("width=" + width.value);
//        System.out.println("height=" + height.value);
//        System.out.println("originalWidth=" + originalWidth.value);
//        System.out.println("originalHight=" + originalHeight.value);
//        System.out.println("originalDpi=" + originalDpi.value);
        return imageData.value;
    }
    
    
    public byte[] getScaledImage(String sid, String uri, String query) {
    	return this.digilibScaler.getScaledImage(uri, query);
    }

//    /**
//     * Method reading url to trigger event
//     */
//    public void status()
//    {
//        if (UrlHelper.getParameterBoolean("init"))
//        {
//            loadCollection();
//            ((AuthorizationBean)BeanHelper.getSessionBean(AuthorizationBean.class)).init(collection);
//            this.error = false;
//            this.success = false;
//            this.msg = "";
//        }
//        else if ("itemlist".equals(UrlHelper.getParameterValue("start")))
//        {
//            this.error = false;
//            this.success = false;
//            this.msg = "";
//            try
//            {
//                IngestController ic = new IngestController(session.getUser(), collection);
//                ic.ingest(upload(), null);
//                this.success = true;
//            }
//            catch (JAXBException e) {
//                logger.error("Error parsing item metadata. ", e);
//                error = true;
//                if (e.getLinkedException() != null)
//                {
//                    logger.error("Error parsing item metadata. ", e);
//                    error = true;
//                    SAXParseException se = (SAXParseException) e.getLinkedException();
//                    this.msg = se.getMessage();
//                }
//                else
//                {
//               	 this.msg = e.getMessage(); 
//                }
//			} 
//           catch (SAXParseException e) {
//               logger.error("Error parsing item metadata. ", e);
//               error = true;
//               this.msg = e.getMessage();
//			}
//            catch (ClassCastException e) {
//                logger.error("Item metadata not valid. ", e);
//                error = true;
//                this.msg = "The metadata of the items are not valid. System failed to convert them to imeji objects.";
// 			}
//           catch (Exception e) {
//               logger.error("Error during the ingest of item metadata. ", e);
//               error = true;
//               this.msg = e.getMessage();
//			}
//        }
//        else if ("profile".equals(UrlHelper.getParameterValue("start")))
//        {
//            this.error = false;
//            this.success = false;
//            this.msg = "";
//            try
//            {
//                IngestController ic = new IngestController(session.getUser(), collection);
//                ic.ingest(null, upload());
//                this.success = true;
//            }
//             catch (JAXBException e) {
//                 logger.error("Error parsing profile. ", e);
//                 error = true;
//                 if (e.getLinkedException() != null)
//                 {
//                     logger.error("Error parsing profile. ", e);
//                     error = true;
//                     SAXParseException se = (SAXParseException) e.getLinkedException();
//                     this.msg = se.getMessage();
//                 }
//                 else
//                 {
//                	 this.msg = e.getMessage(); 
//                 }
//			} 
//            catch (SAXParseException e) {
//                logger.error("Error parsing profile. ", e);
//                error = true;
//                this.msg = e.getMessage();
//			}
//            catch (Exception e) {
//                logger.error("Error during ingest. ", e);
//                error = true;
//                this.msg = e.getMessage();
//			}
//        }
//        else if (UrlHelper.getParameterBoolean("done"))
//        {
//            try
//            {
//                session.getProfileCached().clear();
//            }
//            catch (Exception e)
//            {
//                logger.error("Error during ingest. ", e);
//                error = true;
//                this.msg = e.getMessage();
//            }
//        }
//    }
//
//    public boolean isSuccess() {
//		return success;
//	}
//
//	public void setSuccess(boolean success) {
//		this.success = success;
//	}
//
//	/**
//     * Upload the files for the ingest
//     * 
//     * @return
//     * @throws Exception
//     */
//    public File upload() throws Exception
//    {
//    	
//        try
//        {
//	        HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext()
//	                .getRequest();
//	        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
//	        
//	        if (isMultipart)
//	        {
//	            ServletFileUpload upload = new ServletFileUpload();
//	            // Parse the request
//	            FileItemIterator iter = upload.getItemIterator(req);
//	            while (iter.hasNext())
//	            {
//	                FileItemStream item = iter.next();
//	                if (item != null && item.getName() != null)
//	                {
//	                    file = write2File("itemListXml", item.openStream());
//	                }
//	            }
//	        }
//        }
//        catch (Exception e)
//        {
//            logger.error("Error during ingest. ", e);
//            error = true;
//            this.msg = e.getMessage();
//        }
//        return file;
//    }
//
//    /**
//     * Load the {@link CollectionImeji} for the ingest
//     */
//    private void loadCollection()
//    {
//        if (collectionId != null)
//        {
//            ((ViewCollectionBean)BeanHelper.getSessionBean(ViewCollectionBean.class)).setId(collectionId);
//            ((ViewCollectionBean)BeanHelper.getSessionBean(ViewCollectionBean.class)).init();
//            collection = ((ViewCollectionBean)BeanHelper.getSessionBean(ViewCollectionBean.class)).getCollection();
//        }
//        else
//        {
//            BeanHelper.error(session.getLabel("error") + " No ID in URL");
//        }
//    }
//
//    /**
//     * Write an {@link InputStream} to temp file
//     * 
//     * @param fileName
//     * @param is
//     * @return
//     * @throws Exception
//     */
//    private File write2File(String fileName, InputStream is) throws Exception
//    {
//        File f = new File(System.getProperty("java.io.tmpdir"), fileName);
//        try
//        {
//            OutputStream os = new FileOutputStream(f);
//            try
//            {
//                byte[] buffer = new byte[4096];
//                for (int n; (n = is.read(buffer)) != -1;)
//                {
//                    os.write(buffer, 0, n);
//                }
//            }
//            finally
//            {
//                os.close();
//            }
//        }
//        finally
//        {
//            is.close();
//        }
//        return f;
//    }

    /**
     * getter
     * 
     * @return
     */
    public CollectionImeji getCollection()
    {
        return collection;
    }

    /**
     * setter
     * 
     * @param collection
     */
    public void setCollection(CollectionImeji collection)
    {
        this.collection = collection;
    }

    /**
     * getter
     * 
     * @return
     */
    public String getCollectionId()
    {
        return collectionId;
    }

    /**
     * setter
     * 
     * @param collectionId
     */
    public void setCollectionId(String collectionId)
    {
        this.collectionId = collectionId;
    }

    /**
     * Return the size of the current {@link CollectionImeji}
     * 
     * @return
     */
    public int getCollectionSize()
    {
        return getCollection().getImages().size();
    }

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}


	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
package de.mpg.imeji.rest.process;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.rest.api.AlbumService;
import de.mpg.imeji.rest.api.CollectionService;
import de.mpg.imeji.rest.to.AlbumTO;
import de.mpg.imeji.rest.to.JSONResponse;


public class AlbumProcess {
	
	public static JSONResponse readAlbum(HttpServletRequest req, String id) {
		JSONResponse resp;

		User u = BasicAuthentication.auth(req);

		AlbumService ccrud = new AlbumService();
		try {
			resp = RestProcessUtils.buildResponse(Status.OK.getStatusCode(), ccrud.read(id, u));
		} catch (Exception e) {
			resp = RestProcessUtils.localExceptionHandler(e, e.getLocalizedMessage());
		}
		return resp;

	}

	public static JSONResponse createAlbum(HttpServletRequest req) {
		JSONResponse resp; 

		User u = BasicAuthentication.auth(req);
		
		if (u == null) {
			resp = RestProcessUtils.buildJSONAndExceptionResponse(Status.UNAUTHORIZED.getStatusCode(), CommonUtils.USER_MUST_BE_LOGGED_IN);
		} else {
			AlbumService service = new AlbumService();
			AlbumTO to = (AlbumTO) RestProcessUtils.buildTOFromJSON(req, AlbumTO.class);
			try {
				resp = RestProcessUtils.buildResponse(Status.CREATED.getStatusCode(), service.create(to, u));
			} catch (ImejiException e) {
				resp = RestProcessUtils.localExceptionHandler(e, e.getLocalizedMessage());
			}

		}
		return resp;
	}
	
	public static JSONResponse deleteAlbum(HttpServletRequest req,
			String id) {
		JSONResponse resp;
		User u = BasicAuthentication.auth(req);
		AlbumService service = new AlbumService(); 
		
		try {
			resp= RestProcessUtils.buildResponse(Status.NO_CONTENT.getStatusCode(), service.delete(id, u));
		} catch (Exception e) {
			resp = RestProcessUtils.localExceptionHandler(e, e.getLocalizedMessage());
		}
		return resp;
	}
		


}
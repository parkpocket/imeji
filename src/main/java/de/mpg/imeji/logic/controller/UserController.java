/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.logic.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.mpg.imeji.logic.Imeji;
import de.mpg.imeji.logic.auth.authorization.AuthorizationPredefinedRoles;
import de.mpg.imeji.logic.reader.ReaderFacade;
import de.mpg.imeji.logic.search.Search;
import de.mpg.imeji.logic.search.SearchFactory;
import de.mpg.imeji.logic.search.query.SPARQLQueries;
import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.logic.vo.UserGroup;
import de.mpg.imeji.logic.writer.WriterFacade;
import de.mpg.j2j.exceptions.NotFoundException;

/**
 * Controller for {@link User}
 * 
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public class UserController {
	private static final ReaderFacade reader = new ReaderFacade(Imeji.userModel);
	private static final WriterFacade writer = new WriterFacade(Imeji.userModel);
	private User user;

	/**
	 * User type (restricted: can not create collection)
	 * 
	 * @author saquet
	 *
	 */
	public enum USER_TYPE {
		DEFAULT, ADMIN, RESTRICTED;
	}

	/**
	 * Constructor
	 * 
	 * @param user
	 */
	public UserController(User user) {
		this.user = user;
	}

	/**
	 * Create a new user in the database with predefined roles (ADMIN, DEFAULT
	 * or RESTRICTED)
	 * 
	 * @param newUser
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public User create(User u, USER_TYPE type) throws Exception {
		switch (type) {
		case ADMIN:
			u.setGrants(AuthorizationPredefinedRoles.imejiAdministrator(u
					.getId().toString()));
			break;
		case RESTRICTED:
			u.setGrants(AuthorizationPredefinedRoles.restrictedUser(u.getId()
					.toString()));
			break;
		default:
			u.setGrants(AuthorizationPredefinedRoles.defaultUser(u.getId()
					.toString()));
			break;
		}
		u.setName(u.getPerson().getGivenName() + " "
				+ u.getPerson().getFamilyName());
		writer.create(WriterFacade.toList(u), user);
		return u;
	}

	/**
	 * Delete a {@link User}
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void delete(User user) throws Exception {
		// remove user grant
		writer.delete(new ArrayList<Object>(user.getGrants()), this.user);
		// remove user
		writer.delete(WriterFacade.toList(user), this.user);
	}

	/**
	 * Retrieve a {@link User} according to its email
	 * 
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public User retrieve(String email) throws Exception {
		User u = (User) reader.read(ObjectHelper.getURI(User.class, email)
				.toString(), user, new User());
		UserGroupController ugc = new UserGroupController();
		u.setGroups((List<UserGroup>) ugc.searchByUser(u, user));
		return u;
	}

	/**
	 * Retrieve a {@link User} according to its uri (id)
	 * 
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public User retrieve(URI uri) throws Exception {
		User u = (User) reader.read(uri.toString(), user, new User());
		UserGroupController ugc = new UserGroupController();
		u.setGroups((List<UserGroup>) ugc.searchByUser(u, user));
		return u;
	}

	/**
	 * Update a {@link User}
	 * 
	 * @param updatedUser
	 *            : The user who is updated in the database
	 * @param currentUSer
	 *            : The user who does the update
	 * @throws Exception
	 */
	public void update(User updatedUser, User currentUser) throws Exception {
		updatedUser.setName(updatedUser.getPerson().getGivenName() + " "
				+ updatedUser.getPerson().getFamilyName());
		writer.update(WriterFacade.toList(updatedUser), currentUser);
	}

	/**
	 * Retrieve all {@link User} in imeji<br/>
	 * Only allowed for System administrator
	 * 
	 * @return
	 */
	public Collection<User> retrieveAll(String name) {
		Search search = SearchFactory.create();
		return loadUsers(search.searchSimpleForQuery(
				SPARQLQueries.selectUserAll(name)).getResults());
	}

	public Collection<User> retrieveUserWithGrantFor(String grantFor) {
		Search search = SearchFactory.create();
		return loadUsers(search.searchSimpleForQuery(
				SPARQLQueries.selectUserWithGrantFor(grantFor)).getResults());
	}

	/**
	 * Load all {@link User}
	 * 
	 * @param uris
	 * @return
	 */
	public Collection<User> loadUsers(List<String> uris) {
		Collection<User> users = new ArrayList<User>();
		for (String uri : uris) {
			try {
				users.add((User) reader.read(uri, user, new User()));
			} catch (NotFoundException e) {
				throw new RuntimeException("User " + uri + " not found", e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return users;
	}

	/**
	 * This method checks if a admin user exists for this instance
	 * 
	 * @return true of no admin user exists, false otherwise
	 */
	public static boolean adminUserExist() {
		boolean exist = false;
		Search search = SearchFactory.create();
		List<String> uris = search.searchSimpleForQuery(
				SPARQLQueries.selectUserSysAdmin()).getResults();
		if (uris != null && uris.size() > 0) {
			exist = true;
		}
		return exist;
	}

	/**
	 * Retrieve all admin users
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<User> retrieveAllAdmins() {
		Search search = SearchFactory.create();
		List<String> uris = search.searchSimpleForQuery(
				SPARQLQueries.selectUserSysAdmin()).getResults();
		List<User> admins = new ArrayList<User>();
		for (String uri : uris) {
			try {
				admins.add(retrieve(URI.create(uri)));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return admins;
	}
}

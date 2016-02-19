package de.mpg.imeji.test.logic.auth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.mpg.imeji.testimpl.logic.auth.FileAuthorizationTest;
import de.mpg.imeji.testimpl.logic.auth.HttpAuthenticationTest;
import de.mpg.imeji.testimpl.logic.auth.SimpleAuthenticationTest;
import util.SuperTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({SimpleAuthenticationTest.class, HttpAuthenticationTest.class,
    FileAuthorizationTest.class})
public class AuthTestSuite extends SuperTestSuite {

}

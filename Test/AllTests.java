import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//This file run every test
@RunWith(Suite.class)
@SuiteClasses({ BookTest.class, BorrowTest.class, CreateTest.class, LibrarianActionTest.class, LoginTest.class,
		 })
public class AllTests {
     
}

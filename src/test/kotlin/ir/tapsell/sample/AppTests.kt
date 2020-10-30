package ir.tapsell.sample

import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class AppTests {
	companion object {
		@BeforeClass @JvmStatic @Throws(Exception::class)
		fun setUpBeforeClass() {
			System.setProperty("app.mode.is-test", true.toString())
		}
	}
	@Test
	fun contextLoads() {

	}
}
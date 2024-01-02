package com.learning.type_system

import scala.jdk.CollectionConverters._

object SelfTypes extends App {
  /*
    The self-type annotation in Scala is a way to express dependency between two types.
    If type A depends on type B, then we cannot instantiate an object of A without providing an instance of B.
   */

  case class Test(name: String, assertion: Map[String, String] => Boolean) {
    def execute(env: Map[String, String]): Boolean = {
      println(s"Execute test $name with environment $env")
      assertion.apply(env)
    }
  }

  // Imagine we want to set up a framework to execute tests. We need a type that represents the execution environment
  trait TestEnvironment {
    val envName: String
    def readEnvironmentProperties: Map[String, String]
  }

  // and a type that is the thing that executes the tests
  class TestExecutor { env: TestEnvironment =>
    def execute(tests: List[Test]): Boolean = {
      println(s"Executing test with $envName environment")
      tests.forall(_.execute(readEnvironmentProperties))
    }
  }
  /*
    To run tests, the executor needs an instance of TestEnvironment. In Scala, we can express this constraint using a self-type annotation.
      The notation env: TestEnvironment => declares the dependency from the type TestEnvironment, calling it env.

    The self-type annotation looks like a function as given a set of inputs, it returns a new type.
      In our example, the returned type is the TestExecutor class.
   */

  // We can mix the TestExecutor class only with types that fulfill the dependency with the TestEnvironment trait.
  // Let’s implement a test environment for the Windows operating system:
  trait WindowsTestEnvironment extends TestEnvironment {
    override val envName: String = "Windows"

    override def readEnvironmentProperties: Map[String, String] =
      System.getenv().asScala.toMap
  }

  // If we want to implement a JUnit 5 test executor, we need to provide also a test environment:
  class JUnit5TestExecutor extends TestExecutor with WindowsTestEnvironment {}
  // If we don’t mix a test environment with a test executor, the compiler warns us that the dependency is not satisfied:
  // class JUnit5TestExecutorNew extends TestExecutor

  // We can resolve the declared dependency during the definition of a new type, or we can fix it also during object instantiation:
  val windowTestEnvExecutor: TestExecutor = new TestExecutor with WindowsTestEnvironment

  /*
    - In our example, we call the dependency env. However, we can access its properties using the this reference.
      In the method execute of the class TestExecutor, we access the properties envName and readEnvironmentProperties
      as they are declared directly in the class
    - However, the properties are part of the TestEnvironment trait. This is another peculiarity of the self-type
      annotation – it broadens the scope of the this reference.
  */
  // To avoid shadowing of already-defined properties, we can use the name given to the self-type annotation.
  // For example, imagine we want to decorate our class Test with some logging before and after the execution:
  class TestWithLogging(name: String, assertion: Map[String, String] => Boolean) extends Test(name, assertion) {
    inner: Test =>
    override def execute(env: Map[String, String]): Boolean = {
      println("Before the test")
      val result = inner.execute(env)
      println ("After the test")
      result
    }
  }
  // In this case, we have to associate a name with the dependency. Referring to the inner variable,
  // we can access the execute method of the contained test.
}

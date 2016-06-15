package conformance;

import test.*;

/** Runs all conformance tests on distributed filesystem components.

    <p>
    Tests performed are:
    <ul>
    <li>{@link conformance.rmi.SkeletonTest}</li>
    <li>{@link conformance.rmi.StubTest}</li>
    <li>{@link conformance.rmi.ConnectionTest}</li>
    <li>{@link conformance.rmi.ThreadTest}</li>
    </ul>
 */
public class ConformanceTests
{
    /** Runs the tests.

        @param arguments Ignored.
     */
    public static void main(String[] arguments)
    {
        // Create the test list, the series object, and run the test series.
        @SuppressWarnings("unchecked")
        Class<? extends Test>[]     tests =
            new Class[] {conformance.rmi.CallTest.class,
                         conformance.rmi.ArgumentTest.class,
                         conformance.rmi.ReturnTest.class,
                         conformance.rmi.ExceptionTest.class,
                         conformance.rmi.CompleteCallTest.class,
                         conformance.rmi.ImplicitStubCallTest.class,
                         conformance.rmi.NullTest.class,
                         conformance.rmi.RemoteInterfaceTest.class,
                         conformance.rmi.ListenTest.class,
                         conformance.rmi.RestartTest.class,
                         conformance.rmi.NoAddressTest.class,
                         conformance.rmi.ServiceErrorTest.class,
                         conformance.rmi.StubTest.class,
                         conformance.rmi.EqualsTest.class,
                         conformance.rmi.HashCodeTest.class,
                         conformance.rmi.ToStringTest.class,
                         conformance.rmi.SerializableTest.class,
                         conformance.rmi.OverloadTest.class,
                         conformance.rmi.ShadowTest.class,
                         conformance.rmi.InheritanceTest.class,
                         conformance.rmi.SubclassTest.class,
                         conformance.rmi.SecurityTest.class,
                         conformance.rmi.ThreadTest.class};

        Series                      series = new Series(tests);
        SeriesReport                report = series.run(10, System.out); //originally timeout = 3

        // Print the report and exit with an appropriate exit status.
        report.print(System.out);
        System.exit(report.successful() ? 0 : 2);
    }
}

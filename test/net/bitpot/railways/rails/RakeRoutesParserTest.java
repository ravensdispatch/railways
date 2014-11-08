package net.bitpot.railways.rails;


import net.bitpot.railways.models.RailsEngine;
import net.bitpot.railways.models.Route;
import net.bitpot.railways.models.RouteList;
import net.bitpot.railways.models.routes.RequestMethod;
import net.bitpot.railways.parser.RailsRoutesParser;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Tests for RailsRoutesParser.
 */
public class RakeRoutesParserTest
{
    RailsRoutesParser parser = null;

    @Before
    public void setUp() throws Exception
    {
        parser = new RailsRoutesParser();
    }


    @Test
    public void testParse() throws Exception
    {
        RouteList routes = parser.parseFile("test/data/parserTest_1.txt");

        assertNotNull(routes);
        assertEquals(routes.size(), 6);
    }


    @Test
    public void testStrErrorParsing() throws IOException
    {
        String stdErr = readFile("test/data/sample_stderr.txt");

        parser.parseErrors(stdErr);
        String stack = parser.getErrorStacktrace();

        assertTrue(stack.length() > 0);
        assertEquals(RailsRoutesParser.ERROR_GENERAL, parser.getErrorCode());
        assertTrue(parser.isErrorReported());
        assertFalse("Rake error wasn't cleaned from unnecessary text.",
                stack.contains("rake aborted!"));
    }


    @Test
    public void testStdErrorParserRemovesRakeMessages()
    {
        String s = "** Invoke routes (first_time)\n" +
                   "** Invoke environment (first_time)";

        parser.parseErrors(s);
        String stack = parser.getErrorStacktrace();

        assertEquals(stack.length(), 0);
        assertFalse(parser.isErrorReported());
        assertEquals(RailsRoutesParser.NO_ERRORS, parser.getErrorCode());
    }


    @Test
    public void testWrongRakeTaskCall() {
        String s = "rake aborted!\n" +
                "    Don't know how to build task 'xroutes'";

        parser.parse("", s);

        assertEquals(RailsRoutesParser.ERROR_RAKE_TASK_NOT_FOUND, parser.getErrorCode());
        assertTrue(parser.isErrorReported());
    }


    @Test
    public void testConstraintsParsing()
    {
        String rails3routeConstraints = "           GET    /users/:id(.:format)                   users#show {:id=>/[A-Za-z]{3,}/}";

        // Test constraints parsing
        List<Route> routeList = parser.parseLine(rails3routeConstraints);
        Route route = routeList.get(0);

        assertEquals(route.getAction(), "show");
    }


    @Test
    public void testIsInvalidRouteLine()
    {
        String rails4routeHeader = "   Prefix Verb   URI Pattern               Controller#Action";
        String rails4routeLine = "edit_user GET    /users/:id/edit(.:format) users#edit";
        assertTrue(parser.parseSpecialLine(rails4routeHeader));
        assertFalse(parser.parseSpecialLine(rails4routeLine));
    }


    @Test
    public void testParsingMultipleRouteTypesInASingleLine() {
        String line = "  test GET|POST /test(.:format)             clients#show  ";

        List<Route> routes = parser.parseLine(line);
        assertEquals("Resulting routes count", 2, routes.size());


        // Test first route
        Route expected = new Route(null, RequestMethod.GET,
                "/test(.:format)", "clients", "show", "test");
        Route actual = routes.get(0);

        TestUtils.assertRouteEquals(expected, actual);


        // Test second route
        expected = new Route(null, RequestMethod.POST,
                "/test(.:format)", "clients", "show", "test");
        actual = routes.get(1);

        TestUtils.assertRouteEquals(expected, actual);
    }


    @Test
    public void testParsingEngineRoutes() throws Exception {
        parser.parseFile("test/data/engine_routes_parsing.data.txt");

        List<RailsEngine> enginesList = parser.getMountedEngines();
        assertEquals(enginesList.size(), 2);

        RailsEngine engine = enginesList.get(0);
        assertEquals("RailsAdmin::Engine", engine.getEngineClassName());
        assertEquals("/admin", engine.getRouteNamespace());

        engine = enginesList.get(1);
        assertEquals("RailsSettingsUi::Engine", engine.getEngineClassName());
        assertEquals("/settings", engine.getRouteNamespace());
    }


    @Test
    public void testParsingEngineRoutesOrderAndContents() throws Exception {
        RouteList routes = parser.parseFile("test/data/engine_routes_parsing.data.txt");

        // Engine routes should follow parent engine route which mounts them
        Route route = routes.get(1);
        assertEquals("rails_admin/main#dashboard", route.getActionText());
        assertEquals("/admin", route.getPath());

        route = routes.get(2);
        assertEquals("rails_admin/main#index", route.getActionText());
        assertEquals("/admin/:model_name(.:format)", route.getPath());

        route = routes.get(3);
        assertEquals("rails_settings_ui/settings#index", route.getActionText());
        assertEquals("/settings", route.getPath());

        route = routes.get(4);
        assertEquals("rails_settings_ui/settings#update_all", route.getActionText());
        assertEquals("/settings/update_all(.:format)", route.getPath());
    }


    private String readFile( String file ) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader (file));
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        String         line;

        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }
}
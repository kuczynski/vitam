package fr.gouv.vitam.core.database.collections.translator.mongodb;

import static fr.gouv.vitam.builder.request.construct.QueryHelper.mlt;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.prefix;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.search;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.gouv.vitam.builder.request.construct.Select;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.QUERY;
import fr.gouv.vitam.builder.request.construct.query.MatchQuery;
import fr.gouv.vitam.builder.request.construct.query.MltQuery;
import fr.gouv.vitam.builder.request.construct.query.Query;
import fr.gouv.vitam.builder.request.construct.query.SearchQuery;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.parser.request.parser.SelectParser;

@SuppressWarnings("javadoc")
public class QueryToMongodbTest {
    private static final String exampleMd = "{ $roots : [ 'id0' ], $query : [ " + "{ $path : [ 'id1', 'id2'] }," +
        "{ $and : [ " + "{$exists : 'mavar1'}, " + "{$missing : 'mavar2'}, " + "{$isNull : 'mavar3'}, " + "{ $or : [ " +
        "{$in : { 'mavar4' : [1, 2, 'maval1'] }}, " + "{ $nin : { 'mavar5' : ['maval2', true] } } ] } ] }," +
        "{ $not : [ " + "{ $size : { 'mavar5' : 5 } }, " + "{ $gt : { 'mavar6' : 7 } }, " +
        "{ $lte : { 'mavar7' : 8 } } ] , $exactdepth : 4}," + "{ $not : [ " + "{ $eq : { 'mavar8' : 5 } }, " +
        "{ $ne : { 'mavar9' : 'ab' } }, " + "{ $wildcard : { 'mavar9' : 'ab' } }, " +
        "{ $range : { 'mavar10' : { $gte : 12, $lte : 20} } } ], $depth : 1}, " +
        "{ $and : [ { $term : { 'mavar14' : 'motMajuscule', 'mavar15' : 'simplemot' } } ] }, " +
        "{ $regex : { 'mavar14' : '^start?aa.*' }, $depth : -1 } " + "], " +
        "$filter : {$offset : 100, $limit : 1000, $hint : ['cache'], " +
        "$orderby : { maclef1 : 1 , maclef2 : -1,  maclef3 : 1 } }," +
        "$projection : {$fields : {#dua : 1, #all : 1}, $usage : 'abcdef1234' } }";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    private Select createSelect() {
        try {
            final SelectParser request1 = new SelectParser();
            request1.parse(exampleMd);
            assertNotNull(request1);
            return request1.getRequest();
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return null;
        }
    }

    @Test
    public void testGetCommands() {
        try {
            final Select select = createSelect();
            final Bson bsonRoot = QueryToMongodb.getRoots("_up", select.getRoots());
            final List<Query> list = select.getQueries();
            for (int i = 0; i < list.size(); i++) {
                System.out.println(i + " = " + list.get(i).toString());
                final Bson bsonQuery = QueryToMongodb.getCommand(list.get(i));
                final Bson pseudoRequest = QueryToMongodb.getFullCommand(bsonQuery, bsonRoot);
                System.out.println(i + " = " + MongoDbHelper.bsonToString(pseudoRequest, false));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetMultiRoots() throws InvalidParseOperationException {
        final String s = "{ $roots: ['id0', 'id1'], $query : [], $filter : [], $projection : [] }";
        final SelectParser request = new SelectParser();
        request.parse(s);
        final Select select = request.getRequest();
        final Bson bsonRoot = QueryToMongodb.getRoots("_up", select.getRoots());
        assertEquals("{ \"_up\" : { \"$in\" : [\"id0\", \"id1\"] } }", MongoDbHelper.bsonToString(bsonRoot, false));
    }

    @Test(expected = InvalidParseOperationException.class)
    public void shouldRaiseException_whenMltIsNotAllowed()
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final Query query = new MltQuery(QUERY.MLT, "var", "val");
        QueryToMongodb.getCommand(query);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void shouldRaiseException_whenPrefixIsNotAllowed()
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final Query query = new MatchQuery(QUERY.PREFIX, "var", "val");
        QueryToMongodb.getCommand(query);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void shouldRaiseException_whenSearchIsNotAllowed()
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final Query query = new SearchQuery(QUERY.SEARCH, "var", "val");
        QueryToMongodb.getCommand(query);
    }

    @Test
    public void testWildcardCase() throws InvalidParseOperationException {
        final String s =
            "{ $roots: [], $query : [{ $wildcard : { 'mavar14' : 'motMajuscule'}}], $filter : [], $projection : [] }";
        final SelectParser request = new SelectParser();
        request.parse(s);
        final Select select = request.getRequest();
        final List<Query> list = select.getQueries();
        final Bson bsonQuery = QueryToMongodb.getCommand(list.get(0));
        assertEquals("{ \"mavar14\" : { \"$regex\" : \"motMajuscule\", \"$options\" : \"\" } }",
            MongoDbHelper.bsonToString(bsonQuery, false));
    }

    @Test(expected = InvalidParseOperationException.class)
    public void testGetCommandsThrowInvalidParseOperationExceptionWithMLT()
        throws InvalidCreateOperationException, InvalidParseOperationException {
        QueryToMongodb.getCommand(mlt("value", "var1", "var2"));
    }

    @Test(expected = InvalidParseOperationException.class)
    public void testGetCommandsThrowInvalidParseOperationExceptionWithPREFIX()
        throws InvalidCreateOperationException, InvalidParseOperationException {
        QueryToMongodb.getCommand(prefix("var1", "var2"));
    }

    @Test(expected = InvalidParseOperationException.class)
    public void testGetCommandsThrowInvalidParseOperationExceptionWithSEARCH()
        throws InvalidCreateOperationException, InvalidParseOperationException {
        QueryToMongodb.getCommand(search("var1", "var2"));
    }

}
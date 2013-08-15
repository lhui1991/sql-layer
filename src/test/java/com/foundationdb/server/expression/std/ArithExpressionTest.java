/**
 * Copyright (C) 2009-2013 Akiban Technologies, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.foundationdb.server.expression.std;

import com.foundationdb.server.error.InvalidArgumentTypeException;
import com.foundationdb.server.types.extract.Extractors;
import com.foundationdb.server.error.OverflowException;
import com.foundationdb.server.error.DivisionByZeroException;
import com.foundationdb.server.expression.Expression;
import com.foundationdb.server.expression.ExpressionComposer;
import com.foundationdb.server.types.AkType;
import com.foundationdb.server.types.ValueSource;
import com.foundationdb.server.types.util.ValueHolder;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArithExpressionTest  extends ComposedExpressionTestBase
{
    protected ArithOp ex =  ArithOps.MINUS;
    private final CompositionTestInfo testInfo = new CompositionTestInfo(2, AkType.DOUBLE, true);

    @Test
    public void testLongModDouble ()
    {
        Expression left = new LiteralExpression(AkType.LONG, 2L);
        Expression right = new LiteralExpression(AkType.DOUBLE, 3.4);
        Expression top = ArithOps.MOD.compose(left, right);

        assertEquals("Top should be DOUBLE ", AkType.DOUBLE, top.valueType());
        assertEquals(2.0, top.evaluation().eval().getDouble(), 0.0001);
    }

    @Test public void testLongModLong ()
    {
        Expression left = new LiteralExpression(AkType.LONG, 2L);
        Expression right = new LiteralExpression(AkType.DOUBLE, 3.4);
        Expression top = ArithOps.MOD.compose(left, right);

        assertEquals("Top should be DOUBLE ", AkType.DOUBLE, top.valueType());
        assertEquals(2.0, top.evaluation().eval().getDouble(), 0.0001);
    }

    @Test (expected=DivisionByZeroException.class)
    public void modWithZero ()
    {
        Expression left = new LiteralExpression(AkType.LONG, 1L);
        Expression right = new LiteralExpression(AkType.FLOAT, 0f);
        Expression top = new ArithExpression(left, ArithOps.MOD, right);

        assertEquals("Top should be FLOAT ", AkType.FLOAT, top.valueType());
        top.evaluation().eval().getFloat();
    }

    @Test
    public void testFloatTimesInterval ()
    {
        Expression left = new LiteralExpression(AkType.FLOAT, 3.5f);
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, 2L);
        Expression top = new ArithExpression(left, ArithOps.MULTIPLY, right);

        assertEquals("Top should be INTERVAL_MONTH ", AkType.INTERVAL_MONTH, top.valueType());
        assertEquals(7L, top.evaluation().eval().getInterval_Month());
    }

    @Test
    public void longMinusFloat ()
    {
        Expression left = new LiteralExpression(AkType.LONG, 3);
        Expression right = new LiteralExpression(AkType.FLOAT,2.5f);
        Expression top = new ArithExpression(left, ex = ArithOps.MINUS, right);

        assertEquals("top should be float", AkType.FLOAT, top.valueType());
        assertEquals(0.5f, top.evaluation().eval().getFloat(), 0.0001);
    }

    @Test
    public void longMinusDouble ()
    {
        Expression left = new LiteralExpression(AkType.LONG, 5L);
        Expression right = new LiteralExpression(AkType.DOUBLE, 2.0);
        Expression top = new ArithExpression(left, ex = ArithOps.MINUS, right);

        assertTrue("top should be constant", top.isConstant());
        ValueSource actual = new ValueHolder(top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.DOUBLE, 3.0);
        assertEquals("ValueSource", expected, actual);

    }

    @Test
    public void bigDecimalTimesBigInteger ()
    {
        Expression left = new LiteralExpression (AkType.DECIMAL, BigDecimal.valueOf(2.0));
        Expression right = new LiteralExpression(AkType.U_BIGINT, BigInteger.ONE);
        Expression top = new ArithExpression(left, ex = ArithOps.MULTIPLY, right);

        assertTrue("Top is const", top.isConstant());
        ValueSource actual = new ValueHolder(top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.DECIMAL, BigDecimal.valueOf(2.0));
        assertEquals("actual == expected", expected, actual);
    }

    @Test
    public void longDivLong ()
    {
        Expression left = new LiteralExpression (AkType.LONG, 2L);
        Expression right = new LiteralExpression(AkType.LONG, 5L);
        Expression top = new ArithExpression(left, ex = ArithOps.DIV, right);

        assertTrue("Top is const", top.isConstant());
        ValueSource actual = new ValueHolder(top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.LONG, 0L);
        assertEquals("actual == expected", expected, actual);
    }

    @Test
    public void longDivideLong()
    {
        Expression left = new LiteralExpression(AkType.LONG, 2L);
        Expression right = new LiteralExpression(AkType.LONG, 5L);
        Expression top = new ArithExpression(left, ex = ArithOps.DIVIDE, right);

        assertEquals("Top Type", AkType.DOUBLE, top.valueType());
        assertEquals("value ", 0.4d, top.evaluation().eval().getDouble(), 0.0000001);
    }
    
    @Test
    public void doubleDivFloat()
    {
        Expression left = new LiteralExpression(AkType.DOUBLE, 5.0d);
        Expression right = new LiteralExpression(AkType.FLOAT, 2f);
        Expression top = new ArithExpression(left, ArithOps.DIV, right);
        
        assertEquals("Top type ", AkType.DOUBLE, top.valueType());
        assertEquals("value ", 2.0d, top.evaluation().eval().getDouble(), 0.00001);
    }
    
    @Test
    public void doubleDivDecimal()
    {
        Expression left = new LiteralExpression(AkType.DOUBLE, 10.0d);
        Expression right = new LiteralExpression(AkType.DECIMAL, BigDecimal.valueOf(3));
        Expression top = new ArithExpression(left, ArithOps.DIV, right);
        
        assertEquals("Top type ", AkType.DECIMAL, top.valueType());
        assertEquals("value ", BigDecimal.valueOf(3), top.evaluation().eval().getDecimal());
    }
    
    @Test
    public void bigIntPlusBigInt ()
    {
        Expression left = new LiteralExpression (AkType.U_BIGINT, BigInteger.ONE);
        Expression right = new LiteralExpression (AkType.U_BIGINT, BigInteger.TEN);
        Expression top = new ArithExpression (left, ex = ArithOps.ADD, right);

        assertTrue("Top is const", top.isConstant());
        ValueSource actual = new ValueHolder (top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.U_BIGINT, BigInteger.valueOf(11L));
        assertEquals("actual == expected", expected, actual);
    }

    @Test (expected = DivisionByZeroException.class)
    public void divideByZero ()
    {
        Expression left = new LiteralExpression (AkType.U_BIGINT, BigInteger.ONE);
        Expression right = new LiteralExpression (AkType.U_BIGINT, BigInteger.ZERO);
        Expression top = new ArithExpression (left, ex = ArithOps.DIVIDE, right);
        ValueSource actual = new ValueHolder (top.evaluation().eval());
    }

    @Test (expected = InvalidArgumentTypeException.class)
    public void datePlusLong ()
    {
        Expression left = new LiteralExpression (AkType.DATE, 1L);
        Expression right = new LiteralExpression (AkType.LONG, 2L);
        Expression top = new ArithExpression (left, ex = ArithOps.ADD, right);

        assertTrue("Top is const", top.isConstant());
        ValueSource actual = new ValueHolder (top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.LONG, 3L);
        assertEquals("actuall = expected", expected, actual);
    }

    @Test
    public void longPlusString ()
    {
        Expression left = new LiteralExpression (AkType.U_BIGINT, BigInteger.ONE);
        Expression right = new LiteralExpression (AkType.VARCHAR,"2");
        Expression top = new ArithExpression (left, ex = ArithOps.ADD, right);

        assertTrue("Top is const", top.isConstant());
        ValueSource actual = new ValueHolder (top.evaluation().eval());
        ValueSource expected = new ValueHolder(AkType.U_BIGINT, BigInteger.valueOf(3L));
        assertEquals("actual == expected", expected, actual);
    }

    @Test
    public void yearMinusYear ()
    {
        Expression year1 = new LiteralExpression (AkType.YEAR,Extractors.getLongExtractor(AkType.YEAR).getLong("2006"));
        Expression year2 = new LiteralExpression (AkType.YEAR,Extractors.getLongExtractor(AkType.YEAR).getLong("1991"));
        Expression interval = new ArithExpression (year1, ex = ArithOps.MINUS, year2);

        Expression datetime = new LiteralExpression (AkType.DATETIME, 19960229123010L); // 1996-02-29 12:30:10
        Expression rst = new ArithExpression (datetime, ex = ArithOps.ADD, interval);

        // 2006 minus 1991 plus 1996-02-29 12:30:10 equals 2011-03-01 12:30:10L (1996 is a leap year, while 2011 is NOT)
        assertEquals("2011-03-01 12:30:10",
                Extractors.getLongExtractor(AkType.DATETIME).asString(rst.evaluation().eval().getDateTime()));
    }


    @Test
    public void dateMinusDatePlusDate ()
    {
        Expression date1 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2006-11-07"));
        Expression date2 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2006-11-06"));
        Expression interval = new ArithExpression (date1, ex = ArithOps.MINUS, date2);

        Expression date08 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2008-02-28"));
        Expression rst1 = new ArithExpression(interval, ex = ArithOps.ADD, date08);

        Expression date09 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2009-02-28"));
        Expression rst2 = new ArithExpression(interval, ex = ArithOps.ADD, date09);

        // 2006-11-07 minus 2006-11-06 plus 2006-12-12 equals 2008-02-29 (2008 is a leap year)
        assertEquals("2008-02-29", Extractors.getLongExtractor(AkType.DATE).asString(rst1.evaluation().eval().getDate()));

        // 2006-11-07 minus 2006-11-06 plus 2006-12-12 equals 2009-03-01 (2009 is NOT a leap year)
        assertEquals("2009-03-01", Extractors.getLongExtractor(AkType.DATE).asString(rst2.evaluation().eval().getDate()));
    }

    @Test
    public void testDateMinusDatePlusTime ()
    {

        // date - date => interval
        Expression date1 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2006-10-07"));
        Expression date2 = new LiteralExpression (AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2006-09-07"));
        Expression interval = new ArithExpression(date1, ex = ArithOps.MINUS, date2);

        // time + interval => time
        Expression left = new LiteralExpression (AkType.TIME, 123010L);
        Expression top = new ArithExpression (left, ex = ArithOps.ADD, interval);

        assertTrue("Top is const", top.isConstant());

        // 12:30:10 plus (2006-11-07 minus 2006-11-06) equals 12:30:10 => time doesn't change
        assertEquals(123010L, top.evaluation().eval().getTime());
    }

    @Test
    public void testTimeMinusTimePlusDatetime ()
    {
        Expression time1 = new LiteralExpression (AkType.TIME, Extractors.getLongExtractor(AkType.TIME).getLong("12:30:25"));
        Expression time2 = new LiteralExpression (AkType.TIME, Extractors.getLongExtractor(AkType.TIME).getLong("12:30:30"));
        Expression interval = new ArithExpression(time2, ex = ArithOps.MINUS, time1); // interval of 5 secs

        Expression datetime1 = new LiteralExpression (AkType.DATETIME, 20080228235956L); // 2008-02-28 23:59:56
        Expression datetime2 = new LiteralExpression (AkType.DATETIME, 20090228235956L); // 2009-02-28 23:59:56
        Expression rst1 = new ArithExpression(datetime1, ex = ArithOps.ADD, interval);
        Expression rst2 = new ArithExpression(datetime2, ex = ArithOps.ADD, interval);

        // 2008-02-28 23:59:56 plus 5 secs equals 2008-02-29 00:00:01  (2008 is a leap year)
        assertEquals(20080229000001L, rst1.evaluation().eval().getDateTime());

        // 2009-02-28 23:59:56 plus 5 secs equals 2009-03-01 00:00:01  (2009 is NOT a leap year)
        assertEquals(20090301000001L, rst2.evaluation().eval().getDateTime());
    }

    @Test (expected = OverflowException.class)
    public void bigIntPlusDouble () // expect exception
    {
        Expression left = new LiteralExpression(AkType.U_BIGINT, BigInteger.valueOf(Long.MAX_VALUE).pow(100));

        Expression right = new LiteralExpression (AkType.DOUBLE, 100000000.0);
        Expression top = new ArithExpression(left, ex = ArithOps.ADD, right);

        ValueSource actual = new ValueHolder(top.evaluation().eval());
    }

      @Test
    public void testBug912261 ()
    {
        testAddMonth("2000-10-02", 2L, "2000-12-02");
    }

    @Test
    public void testAddMul12Months ()
    {
        int year = 2000, month = 1, day = 12;
        int d = 12;

        // test adding a multiple of 12 months
        // starting with "2000-01-12" to "2000-12-12"
        for (; month <= 12; ++month)
            for (int n = 0; n <= 4; ++n)
                testAddMonth(year + "-" + month + "-" + day,
                        d * n, (year + n) + "-" + String.format("%02d", month) + "-" + day);
    }

    @Test
    public void testAddMul13Months ()
    {   
        testAddMonth("2004-02-29", 0L, "2004-02-29");
        testAddMonth("2004-01-30", 13L, "2005-02-28");
        testAddMonth("2002-03-31", 26L, "2004-05-31");
        testAddMonth("2001-04-30", 39L, "2004-07-30");
        testAddMonth("2003-05-31", 52L, "2007-09-30");
    }

    private static void testAddMonth (String date, long month, String expectedDate)
    {
        Expression left = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong(date));
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, month);

        Expression top = new ArithExpression(left, ArithOps.ADD, right);
        String actual = Extractors.getLongExtractor(AkType.DATE).asString(top.evaluation().eval().getDate());

        assertEquals(date + " + " + month + " MONTHS :",expectedDate, actual);
    }
    
    @Test
    public void testDatePlusIntervalMonth ()
    {
        Expression left = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2008-02-29"));
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, 12L);

        Expression top = new ArithExpression(left, ex = ArithOps.ADD, right);
        String actual = Extractors.getLongExtractor(AkType.DATE).asString(top.evaluation().eval().getDate());

        assertEquals("2009-02-28", actual);
    }

    @Test
    public void testLeapYearChecking ()
    {
        Expression left_1900 = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("1900-01-29"));
        Expression left_2000 = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2000-01-29"));
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, 1L);

        Expression top_1900 = new ArithExpression(left_1900, ex = ArithOps.ADD, right);
        Expression top_2000 = new ArithExpression(left_2000, ex = ArithOps.ADD, right);

        String actual_1900 = Extractors.getLongExtractor(AkType.DATE).asString(top_1900.evaluation().eval().getDate());
        String actual_2000 = Extractors.getLongExtractor(AkType.DATE).asString(top_2000.evaluation().eval().getDate());

        assertEquals("1900-02-28", actual_1900); // 1900 is NOT a leap year
        assertEquals("2000-02-29", actual_2000); // 2000 is a leap year
    }

    @Test
    public void testDateMinusNegativeInterval ()
    {
        Expression left = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2008-01-30"));
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, -13L);

        Expression top = new ArithExpression(left, ex = ArithOps.MINUS, right);
        String actual = Extractors.getLongExtractor(AkType.DATE).asString(top.evaluation().eval().getDate());

        assertEquals("2009-02-28", actual);
    }

    @Test
    public void testDatePlusNegativeIntervalMonth ()
    {
        Expression left = new LiteralExpression(AkType.DATE, Extractors.getLongExtractor(AkType.DATE).getLong("2009-03-31"));
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, -4L);

        Expression top = new ArithExpression(left, ex = ArithOps.ADD, right);
        String actual = Extractors.getLongExtractor(AkType.DATE).asString(top.evaluation().eval().getDate());

        assertEquals("2008-11-30", actual);
    }

    @Test
    public void testDateTimeMinusIntervalMonth ()
    {
        Expression left = new LiteralExpression (AkType.DATETIME, 20090331123010L);
        Expression right = new LiteralExpression(AkType.INTERVAL_MONTH, 4L);

        Expression top = new ArithExpression(left, ex = ArithOps.MINUS, right);
        String actual = Extractors.getLongExtractor(AkType.DATETIME).asString(top.evaluation().eval().getDateTime());

        assertEquals("2008-11-30 12:30:10", actual);
    }

    @Override
    protected ExpressionComposer getComposer()
    {
        return (ExpressionComposer)ex;
    }

    @Override
    protected CompositionTestInfo getTestInfo()
    {
        return testInfo;
    }

    @Override
    public boolean alreadyExc()
    {
        return false;
    }
}
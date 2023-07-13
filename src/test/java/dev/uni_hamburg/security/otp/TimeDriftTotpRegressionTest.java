/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.uni_hamburg.security.otp;

import dev.uni_hamburg.security.otp.api.Clock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TimeDriftTotpRegressionTest {

    private final static Logger LOGGER = Logger.getLogger(TimeDriftTotpRegressionTest.class.getName());

    @Mock
    protected Clock clock;
    protected TimeDriftTotp totp;
    protected String sharedSecret = "B2374TNIQ3HKC446";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(0));
        totp = new TimeDriftTotp(sharedSecret, clock);
    }

    protected long addElapsedTime(int seconds) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        // To produce deterministic results we fix the base to 0s and 0ms
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        LOGGER.info("Current time: " + calendar.getTime());
        calendar.add(Calendar.SECOND, seconds);
        LOGGER.info("Updated time (+" + seconds + "): " + calendar.getTime());
        long currentTimeSeconds = calendar.getTimeInMillis() / 1000;
        return currentTimeSeconds / 30;
    }

    @Test
    public void testUri() throws Exception {
        String name = "john";
        String url = String.format("otpauth://totp/%s?secret=%s", name, sharedSecret);
        assertEquals(url, totp.uri("john"));
    }

    @Test
    public void testUriEncoding() {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret, 0, 0, 0);
        String url = String.format("otpauth://totp/%s?secret=%s", "john%23doe", sharedSecret);
        assertEquals(url, totp.uri("john#doe"));
    }

    @Test
    public void testLeadingZeros() throws Exception {
        final String expected = "002941";

        when(clock.getCurrentInterval()).thenReturn(45187109L);
        String secret = "R5MB5FAQNX5UIPWL";

        TimeDriftTotp totp = new TimeDriftTotp(secret, clock);
        String otp = totp.now();
        assertEquals("Generated token must be zero padded", otp, expected);
    }

    @Test
    public void testCustomInterval() throws Exception {
        Clock customClock = new Clock(20);
        totp = new TimeDriftTotp(sharedSecret, customClock, 0, 0, 0);
        totp.now();
    }

    @Test
    public void testNow() throws Exception {
        String otp = totp.now();
        assertEquals(6, otp.length());
    }

    @Test
    public void testValidOtp() throws Exception {
        String otp = totp.now();
        assertTrue("OTP is not valid", totp.verify(otp));
    }

    @Test
    public void testOtpAfter10seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(10));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(0, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter20seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(20));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(0, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter25seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(25));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(0, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter29seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(29));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(0, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter30seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(30));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter31seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(31));
        assertTrue("OTP should be invalid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter59seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(59));
        assertTrue("OTP should be invalid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpAfter60seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(60));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpAfter61seconds() throws Exception {
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(61));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpLookBack2After29seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(29));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(0, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After30seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(30));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After31seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(31));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After59seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(59));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After60seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(60));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After61seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(61));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After89seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(89));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(-2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookBack2After90seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(90));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpLookBack2After91seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(91));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpLookAhead2Before29seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-29));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookAhead2Before30seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-30));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(1, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookAhead2Before31seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-31));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookAhead2Before59seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-59));
        assertTrue("OTP should be valid", totp.verify(otp));
        assertEquals(2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookAhead2Before60seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-60));
        assertTrue("OTP should be invalid", totp.verify(otp));
        assertEquals(2, totp.getTimeDriftCorrection());
    }

    @Test
    public void testOtpLookAhead2Before61seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-61));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpLookAhead2Before89seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-89));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testOtpLookAhead2Before90seconds() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,2,0);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(addElapsedTime(-90));
        assertFalse("OTP should be invalid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift30Behind() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(0L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(30L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(-30, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift30Ahead() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(30L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(0L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(30, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift20Behind() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(0L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(20L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(-20, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift20Ahead() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(20L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(0L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(20, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift10Behind() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(0L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(10L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(-10, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }

    @Test
    public void testTrainDrift10Ahead() throws Exception {
        TimeDriftTotp totp = new TimeDriftTotp(sharedSecret,clock,0,2);
        when(clock.getCurrentInterval()).thenReturn(10L);
        String otp = totp.now();
        when(clock.getCurrentInterval()).thenReturn(0L);
        assertTrue("OTP should be valid", totp.train(otp));
        assertEquals(10, totp.getTimeDriftCorrection());
        assertTrue("OTP Should be Valid", totp.verify(otp));
    }
}

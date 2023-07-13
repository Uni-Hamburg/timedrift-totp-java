/**
 * Universität Hamburg
 * Copyright Universität Hamburg, and individual contributors
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

public final class TimeDriftTotp extends Totp {
    private final String secret;
    private final int lookAheadInterval;

    private final int lookBehindInterval;

    private final Clock clock;

    /**
     * Worst case time-drift for a TOTP token is estimated at 1s per day.
     * If we assume a Token has been sitting idle for 5 Years we come to
     * the worst case drift of 1825 s (5 * 365).
     *
     * Setting the lookAhead and lookBehind to 60 for training a TOTP Token
     * should include even long deployed Hardware Tokens (Most only have
     * a battery life of 3-7 Yrs)
     */
    private static final int MAX_TIME_DRIFT_TRAINING = 60;

    /**
     * This is the proposed correction for time drifting hardware tokens.
     * This value must be provided by the using application.
     */
    private int timeDriftCorrection;

    public TimeDriftTotp(String secret) {
        this(secret, new Clock());
    }

    public TimeDriftTotp(String secret, Clock clock) {
        // The default values for lookAhead and lookBehind mimic the behavior of
        // aerogear-otp-java. This makes this class a DropIn replacement.
        // aerogear-otp-java only checks current valid code and the past (-1)
        this(secret, clock, 0, 1);
    }

    public TimeDriftTotp(String secret, int lookAheadInterval, int lookBehindInterval) {
        // The default values for lookAhead and lookBehind mimic the behavior of
        // aerogear-otp-java. This makes this class a DropIn replacement
        this(secret, lookAheadInterval, lookBehindInterval, 0);
    }

    public TimeDriftTotp(String secret, int lookAheadInterval, int lookBehindInterval, int timeDriftCorrection) {
        this(secret, new Clock(), lookAheadInterval, lookBehindInterval, timeDriftCorrection);
    }

    public TimeDriftTotp(String secret, Clock clock, int lookAheadInterval, int lookBehindInterval) {
        this(secret, clock, lookAheadInterval, lookBehindInterval, 0);
    }

    public TimeDriftTotp(String secret, Clock clock, int lookAheadInterval, int lookBehindInterval, int timeDriftCorrection) {
        super(secret, clock);
        this.secret = secret;
        this.clock = clock;
        this.lookAheadInterval = lookAheadInterval;
        this.lookBehindInterval = lookBehindInterval;
        this.timeDriftCorrection = timeDriftCorrection;
    }

    private boolean _verify(String otp, boolean train) {
        long code = Long.parseLong(otp);

        long currentInterval = clock.getCurrentInterval();

        int lookAhead = lookAheadInterval + timeDriftCorrection;
        int lookBehind = (-lookBehindInterval) + timeDriftCorrection;
        if (train) {
            lookAhead = MAX_TIME_DRIFT_TRAINING;
            lookBehind = -MAX_TIME_DRIFT_TRAINING;
        }

        for (int i = lookAhead; i >= lookBehind; --i) {
            int candidate = generate(this.secret, currentInterval + i);
            if (candidate == code) {
                this.timeDriftCorrection = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean verify(String otp) {
        return _verify(otp, false);
    }

    public boolean train(String otp) {
        return _verify(otp, true);
    }

    public int getLookAheadInterval() {
        return lookAheadInterval;
    }

    public int getLookBehindInterval() {
        return lookBehindInterval;
    }

    public int getTimeDriftCorrection() {
        return timeDriftCorrection;
    }
}
/**
 * Copyright © 2026 marioogg <a href="https://github.com/marioogg">https://github.com/marioogg</a>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * @author marioogg
 * @since 30-06-2026 18:01
 */
package me.marioogg.mlogin.core.protocol;

public enum ResponseReason {
    OK,
    WRONG_PASSWORD,
    ALREADY_REGISTERED,
    NOT_REGISTERED,
    ALREADY_LOGGED_IN,
    LOGIN_REQUIRED,
    REGISTER_REQUIRED,
    TIMEOUT,
    RATE_LIMITED,
    ERROR
}
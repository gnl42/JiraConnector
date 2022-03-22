/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.cfg;

/**
 * User: pmaruszak
 */
public class UserCfg implements User {
	private String username = "";
	private String password = "";
	private boolean passwordStored;

	public UserCfg(String username, String password, final boolean passwordStored) {
		this.username = username;
		this.password = password;
		this.passwordStored = passwordStored;
	}

	public UserCfg(String username, String password) {
		this(username, password, false);
	}

	public UserCfg() {
	}

	public UserCfg getClone() {
		return new UserCfg(username, password, passwordStored);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String userName) {
		this.username = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}


	public void setPasswordStored(final boolean passwordStored) {
		this.passwordStored = passwordStored;

	}

	public boolean isPasswordStored() {
		return passwordStored;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserCfg userCfg = (UserCfg) o;

        if (passwordStored != userCfg.passwordStored) {
            return false;
        }
        if (password != null ? !password.equals(userCfg.password) : userCfg.password != null) {
            return false;
        }
        if (username != null ? !username.equals(userCfg.username) : userCfg.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (passwordStored ? 1 : 0);
        return result;
    }
}

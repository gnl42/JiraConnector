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

import com.atlassian.theplugin.commons.ServerType;

public class CrucibleServerCfg extends ServerCfg {
	private boolean isFisheyeInstance;
	private static final int HASHCODE_MAGIC = 31;

	// below line does not work as that object is created magically
	// so initialization was moved to the private getter
	//	private FishEyeServer fishEyeView = new FishEyeServer() {...
	private FishEyeServer fishEyeView;

	public CrucibleServerCfg(boolean enabled, String name, ServerIdImpl serverId) {
		super(enabled, name, serverId);
	}

	private FishEyeServer getFishEyeView() {
		if (fishEyeView == null) {
			fishEyeView = new FishEyeServer() {
				public ServerIdImpl getServerId() {
					return CrucibleServerCfg.this.getServerId();
				}
				public String getPassword() {
					return CrucibleServerCfg.this.getPassword();
				}
				public String getName() {
					return CrucibleServerCfg.this.getName();
				}
				public String getUsername() {
					return CrucibleServerCfg.this.getUsername();
				}
				public String getUrl() {
					return CrucibleServerCfg.this.getUrl();
				}
				public boolean isEnabled() {
					return CrucibleServerCfg.this.isEnabled();
				}
				public boolean isUseDefaultCredentials() {
					return CrucibleServerCfg.this.isUseDefaultCredentials();
				}
				public ServerType getServerType() {
					return ServerType.FISHEYE_SERVER;
				}
                public boolean isDontUseBasicAuth() {
                    return false;
                }
                public boolean isUseSessionCookies() {
                    return false;
                }
                public UserCfg getBasicHttpUser() {
                    return null;  
                }
                public boolean isShared() {
                    return false;
                }
                public void setShared(boolean shared) {
                }
            };
		}
		return fishEyeView;
	}

	public CrucibleServerCfg(final String name, final ServerIdImpl serverId) {
		super(true, name, serverId);
	}

	public CrucibleServerCfg(final CrucibleServerCfg other) {
		super(other);
		isFisheyeInstance = other.isFisheyeInstance();
	}

	@Override
	public ServerType getServerType() {
		return ServerType.CRUCIBLE_SERVER;
	}

    public boolean isDontUseBasicAuth() {
        return false;
    }

    public boolean isUseSessionCookies() {
        return false;
    }

    public UserCfg getBasicHttpUser() {
        return null;  
    }

    @Override
	public boolean equals(final Object o) {
		if (!super.equals(o)) {
			return false;
		}

		if (this == o) {
			return true;
		}
		if (!(o instanceof CrucibleServerCfg)) {
			return false;
		}

		final CrucibleServerCfg that = (CrucibleServerCfg) o;

		if (isFisheyeInstance != that.isFisheyeInstance) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = HASHCODE_MAGIC * result + (isFisheyeInstance ? 1 : 0);
		return result;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Crucible Server [");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public CrucibleServerCfg getClone() {
		return new CrucibleServerCfg(this);
	}

	public boolean isFisheyeInstance() {
		return isFisheyeInstance;
	}

	public void setFisheyeInstance(final boolean fisheyeInstance) {
		isFisheyeInstance = fisheyeInstance;
	}

	@Override
	public FishEyeServer asFishEyeServer() {
		if (isFisheyeInstance) {
			return getFishEyeView();
		} else {
			return null;
		}
	}
}

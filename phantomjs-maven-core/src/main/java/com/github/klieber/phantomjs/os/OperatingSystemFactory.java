package com.github.klieber.phantomjs.os;

public class OperatingSystemFactory {

  private static final String OS_NAME = "os.name";
  private static final String OS_ARCH = "os.arch";
  private static final String OS_VERSION = "os.version";

  private LinuxProperties linuxProperties;

  public OperatingSystemFactory() {
    this(new LinuxProperties());
  }

  public OperatingSystemFactory(LinuxProperties linuxProperties) {
    this.linuxProperties = linuxProperties;
  }

  public OperatingSystem create() {
    String name = getSystemProperty(OS_NAME);
    String architecture = getArchitecture();
    String version = getSystemProperty(OS_VERSION);

    return isLinux(name) ? createLinuxOS(name, architecture, version) : createOS(name, architecture, version);
  }

  private String getArchitecture() {
    String arch = getSystemProperty(OS_ARCH);
    String architecture = null;
    if (arch != null) {
      architecture = arch.contains("64") ? "x86_64" : "i686";
    }
    return architecture;
  }

  private boolean isLinux(String name) {
    return name.contains("nux");
  }

  private OperatingSystem createOS(String name,
                                   String architecture,
                                   String version) {
    return new OperatingSystem(
      name,
      architecture,
      version
    );
  }

  private OperatingSystem createLinuxOS(String name,
                                        String architecture,
                                        String version) {
    String distribution = linuxProperties.getDistribution();
    String distributionVersion = linuxProperties.getDistributionVersion();

    return new OperatingSystem(
      name,
      architecture,
      version,
      distribution,
      distributionVersion
    );
  }

  private String getSystemProperty(String name) {
    String property = System.getProperty(name);
    return property != null ? property.toLowerCase() : null;
  }
}

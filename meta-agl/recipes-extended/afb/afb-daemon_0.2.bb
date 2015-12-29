SUMMARY = "HTTP REST interface to automotive backends for HTML5 UI support"
DESCRIPTION = "Automotive-Framework-Binder Daemon provides a HTTP REST \
interface to various automotive-oriented plugins (sound, radio...), \
allowing HTML5 UIs to send platform-specific requests in a secure way."
HOMEPAGE = "http://www.iot.bzh"

LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://src/main.c;endline=17;md5=6cb04bdb88e11107e3af4d8e3f301be5"

DEPENDS = "file json-c libmicrohttpd util-linux dbus alsa-lib rtl-sdr"

SRC_URI = "git://github.com/iotbzh/afb-daemon;protocol=https;branch=master \
           file://afb-daemon-test \
          "
SRCREV = "93daafca4d0419707cdcde181180a689a7fccbfb"
S = "${WORKDIR}/git"

inherit cmake pkgconfig

do_configure_prepend () {
    # CMake 3+ does not behave like 2.8
    sed -i 's,${CMAKE_INSTALL_PREFIX}/,,' ${S}/CMakeLists.txt
}

do_install_append() {
  mkdir -p ${D}/${bindir}
  install -m 0755 ${WORKDIR}/afb-daemon-test ${D}/${bindir}/afb-daemon-test
}

PACKAGES += "${PN}-meta"
ALLOW_EMPTY_${PN}-meta = "1"

PACKAGES_DYNAMIC = "${PN}-plugin-*"

python populate_packages_prepend () {
    afb_libdir = d.expand('${libdir}/afb')
    postinst = d.getVar('plugin_postinst', True)
    pkgs = []
    pkgs_dbg = []

    pkgs += do_split_packages(d, afb_libdir, '(.*)-api\.so$', d.expand('${PN}-plugin-%s'), 'AFB plugin for %s', postinst=postinst, extra_depends=d.expand('${PN}'))
    pkgs_dbg += do_split_packages(d, oe.path.join(afb_libdir, ".debug"), '(.*)-api\.so$', d.expand('${PN}-plugin-%s-dbg'), 'AFB plugin for %s, debug info', postinst=postinst, extra_depends=d.expand('${PN}'))

    metapkg = d.getVar('PN', True) + '-meta'
    d.setVar('RDEPENDS_' + metapkg, ' '.join(pkgs))
}
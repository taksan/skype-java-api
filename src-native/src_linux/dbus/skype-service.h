#ifndef _SKYPE_SERVICE_H_
#define _SKYPE_SERVICE_H_

// Ref:http://library.gnome.org/devel/gobject/unstable/chapter-gobject.html
//     http://live.gnome.org/DBusGlibBindings

#include <glib.h>
#include <dbus/dbus-glib.h>
#include <dbus/dbus-glib-bindings.h>
#include <dbus/dbus-glib-lowlevel.h>

// DBus path and interface
#define SKYPE_SERVICE_PATH       "/com/Skype/Client"
#define SKYPE_SERVICE_INTERFACE  "com.Skype.API.Client"
        
// Service Object
#define TYPE_SKYPE_SERVICE            (skype_service_get_type ())
#define SKYPE_SERVICE(object)         (G_TYPE_CHECK_INSTANCE_CAST ((object), TYPE_SKYPE_SERVICE, SkypeService))
#define SKYPE_SERVICE_CLASS(klass)    (G_TYPE_CHECK_CLASS_CAST ((klass), TYPE_SKYPE_SERVICE, SkypeServiceClass))
#define IS_SKYPE_SERVICE(object)      (G_TYPE_CHECK_INSTANCE_TYPE ((object), TYPE_SKYPE_SERVICE))
#define IS_SKYPE_SERVICE_CLASS(klass) (G_TYPE_CHECK_CLASS_TYPE ((klass), TYPE_SKYPE_SERVICE))
#define SKYPE_SERVICE_GET_CLASS(obj)  (G_TYPE_INSTANCE_GET_CLASS ((obj), TYPE_SKYPE_SERVICE, SkypeServiceClass))

G_BEGIN_DECLS

typedef struct _SkypeService SkypeService;
typedef struct _SkypeServiceClass SkypeServiceClass;

struct _SkypeService {
    GObject parent;
};

struct _SkypeServiceClass {
    GObjectClass parent;
};

gboolean skype_service_notify_callback(SkypeService *object, gchar *message, GError **error);

SkypeService *skype_service_new (void);
GType skype_service_get_type (void);

G_END_DECLS

#endif

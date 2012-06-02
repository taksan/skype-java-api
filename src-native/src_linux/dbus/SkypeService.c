#include "skype-service.h"

G_DEFINE_TYPE(SkypeService, skype_service, G_TYPE_OBJECT)

static void skype_service_finalize (GObject *object)
{
    G_OBJECT_CLASS (skype_service_parent_class)->finalize (object);
}

static void skype_service_class_init (SkypeServiceClass *klass)
{
    GObjectClass *object_class;
    object_class = G_OBJECT_CLASS (klass);
    object_class->finalize = skype_service_finalize;
}

static void skype_service_init (SkypeService *object)
{
    ;
}

SkypeService *skype_service_new (void)
{
    return g_object_new(TYPE_SKYPE_SERVICE, NULL);
}

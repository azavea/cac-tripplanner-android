package com.gophillygo.app;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Glide expects this module to be defined, even if it overrides nothing.
 * See: http://bumptech.github.io/glide/doc/configuration.html
 */

@SuppressWarnings("WeakerAccess")
@GlideModule
public class GpgAppGlideModule extends AppGlideModule {
}

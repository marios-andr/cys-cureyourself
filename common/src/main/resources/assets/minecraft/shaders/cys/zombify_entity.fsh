#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#ifdef DISSOLVE
 uniform sampler2D DissolveMaskSampler;
#endif

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#ifdef PER_FACE_LIGHTING
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
#else
in vec4 vertexColor;
#endif

#ifndef EMISSIVE
 in vec4 lightMapColor;
#endif

#ifndef NO_OVERLAY
 in vec4 overlayColor;
#endif

in vec2 texCoord0;

out vec4 fragColor;

// ---------------------------ZOMBIFY HELPERS-------------------------------------

vec3 rgb2hsv(vec3 c) {
vec4 K = vec4(0.0, - 1.0 /3.0, 2.0 / 3.0, - 1.0);
vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
float d = q.x - min(q.w, q.y);
float e = 1.0e-10;
return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c) {
vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 zombify(vec3 rgb) {
const float SKIN_HUE_CENTER = 20.0 / 360.0;
const float SKIN_HUE_HALFBAND = 25.0 / 360.0;
const float SKIN_MIN_SAT = 0.15;
const float SKIN_MIN_VAL = 0.10;

// Transform offsets
const float HUE_SHIFT = 82.0 / 360.0;
const float SAT_DELTA = - 0.08;
const float VAL_DELTA = - 0.08;

vec3 hsv = rgb2hsv(rgb);

// Circular hue distance
float hueDiff = abs(mod(hsv.x - SKIN_HUE_CENTER + 0.5, 1.0) - 0.5);

bool isSkin = (hueDiff <= SKIN_HUE_HALFBAND)
&& (hsv.y >= SKIN_MIN_SAT)
&& (hsv.z        >= SKIN_MIN_VAL);

if (!isSkin) return rgb;

hsv.x = mod(hsv.x + HUE_SHIFT, 1.0);
hsv.y = clamp(hsv.y + SAT_DELTA, 0.0, 1.0);
hsv.z = clamp(hsv.z + VAL_DELTA, 0.0, 1.0);

return hsv2rgb(hsv);
}

// ---------------------------END OF HELPERS---------------------------

void main() {
vec4 color = texture(Sampler0, texCoord0);
#ifdef ALPHA_CUTOUT
 if (color.a < ALPHA_CUTOUT) {
discard;
}
#endif

color.rgb = zombify(color.rgb);

#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront: vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif

#ifdef DISSOLVE
 if (faceVertexColor.a < texture(DissolveMaskSampler, texCoord0).a) {
discard;
}
// The dissolve effect entirely replaces translucency
faceVertexColor.a = 1.0;
#endif

color *= faceVertexColor * ColorModulator;
#ifndef NO_OVERLAY
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
#endif
#ifndef EMISSIVE
    color *= lightMapColor;
#endif

fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
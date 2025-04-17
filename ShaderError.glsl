#version 460 core
#define OR_IN_OUT
#define OR_GL

// <primitive-types> (ShadeStyleGLSL.kt)
#define d_vertex_buffer 0
#define d_image 1
#define d_circle 2
#define d_rectangle 3
#define d_font_image_map 4
#define d_expansion 5
#define d_fast_line 6
#define d_mesh_line 7
#define d_point 8
#define d_custom 9
#define d_primitive d_expansion
// </primitive-types>




#ifdef OR_GL    
layout(origin_upper_left) in vec4 gl_FragCoord;
#endif   

// <drawer-uniforms(true, true)> (ShadeStyleGLSL.kt)
            
layout(shared) uniform ContextBlock {
    uniform mat4 u_modelNormalMatrix;
    uniform mat4 u_modelMatrix;
    uniform mat4 u_viewNormalMatrix;
    uniform mat4 u_viewMatrix;
    uniform mat4 u_projectionMatrix;
    uniform float u_contentScale;
    uniform float u_modelViewScalingFactor;
    uniform vec2 u_viewDimensions;
};
            
layout(shared) uniform StyleBlock {
    uniform vec4 u_fill;
    uniform vec4 u_stroke;
    uniform float u_strokeWeight;
    uniform float[25] u_colorMatrix;
};
// </drawer-uniforms>
in vec2 va_position;
in vec2 va_texCoord0;
in float va_vertexOffset;

// <transform-varying-in> (ShadeStyleGLSL.kt)
in vec3 v_worldNormal;
in vec3 v_viewNormal;
in vec3 v_worldPosition;
in vec3 v_viewPosition;
in vec4 v_clipPosition;
flat in mat4 v_modelNormalMatrix;
// </transform-varying-in>
flat in int v_instance;
uniform float strokeMult;
uniform float strokeThr;
uniform float strokeFillFactor;
uniform sampler2D tex;
uniform vec4 bounds;

in vec3 v_objectPosition;
in vec2 v_ftcoord;
out vec4 o_color;



float strokeMask() {
	return min(1.0, (1.0-abs(v_ftcoord.x*2.0-1.0))*strokeMult) * min(1.0, v_ftcoord.y);
	//return pow(min(1.0, (1.0-abs(v_ftcoord.x*2.0-1.0)*strokeMult)) * min(1.0, v_ftcoord.y), 1.0);
    //return smoothstep(0.0, 1.0, (1.0-abs(v_ftcoord.x*2.0-1.0))*strokeMult) * smoothstep(0.0, 1.0, v_ftcoord.y);
}

// -- fragmentConstants
int c_instance = v_instance;
int c_element = 0;
vec2 c_screenPosition = gl_FragCoord.xy / u_contentScale;
float c_contourPosition = va_vertexOffset;
vec3 c_boundsPosition = vec3(v_objectPosition.xy - bounds.xy, 0.0) / vec3(bounds.zw,1.0);
vec3 c_boundsSize = vec3(bounds.zw, 0.0);

void main(void) {
	float strokeAlpha = strokeMask();

    vec4 x_stroke = u_stroke;
    vec4 x_fill = u_fill;

        
                        vec2 uv = (c_boundsPosition.xy / c_boundsSize.xy) * 2.0 - 1.0;
                        float dist = length(uv);
                
                        // Colors for the water gradient (light center, dark edges)
                        vec4 innerColor = vec4(0.0, 0.6, 0.8, 1.0); // Light blue/turquoise
                        vec4 midColor = vec4(0.0, 0.4, 0.6, 1.0); // Medium blue
                        vec4 outerColor = vec4(0.0, 0.2, 0.4, 1.0); // Dark blue
        
                        // Smooth falloff using distance, creating the gradient effect
                        float smoothDist = smoothstep(0.0, 1.0, dist);
                        vec4 color = mix(innerColor, midColor, smoothDist);
                        color = mix(color, outerColor, smoothDist * smoothDist); // more weight to edges
                
                        // Optional noise for some variation
                        float noise = sin(dist * 20.0) * 0.1; // Noise adds some texture to make it feel like water
                        color += vec4(noise, noise, noise, 0.0); // Add small noise to all channels
        
                        x_fill = color;
            

    vec4 color = mix(x_stroke, x_fill, strokeFillFactor)  * vec4(1.0, 1.0, 1.0, strokeAlpha);
    vec4 result = color;

    if (strokeAlpha < strokeThr) {
	    discard;
	}

    vec4 final = result;
	final = result;
	final.rgb *= final.a;
    o_color = final;
}
// -------------
// shade-style-custom:expansion--1037324553
// created 2025-04-13T12:31:30.173082100
/*
0(112) : error C1038: declaration of "color" conflicts with previous declaration at 0(102)
*/

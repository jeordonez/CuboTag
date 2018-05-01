#version 330 core
        out vec4 fragColor;
        in vec3 fragPos;
        in vec3 Normal;
        in vec2 UV;
        uniform vec3 lightPos;
        uniform vec3 viewPos;
        uniform vec3 lightColor;
        uniform vec3 objectColor;

        uniform sampler2D diffuseTex;
        const float ambientStrength = 0.2;
        void main() {
        // ambient
        vec3 ambient = ambientStrength * lightColor;
        // diffuse
        vec3 norm = normalize(Normal);
        vec3 lightDir = normalize(lightPos - fragPos);
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = diff * lightColor;

        float specularStrength = 0.5;
        vec3 viewDir = normalize(viewPos - fragPos);
        vec3 reflectDir = reflect(-lightDir, norm);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
        vec3 specular = specularStrength * spec * lightColor;        

        //vec3 result = (ambient + diffuse + specular) * vec3(1.0, 1.0, 1.0);
        vec3 result = (ambient + diffuse + specular) * objectColor * texture(diffuseTex, UV).rgb;
        fragColor = vec4(result, 1.0);
        }
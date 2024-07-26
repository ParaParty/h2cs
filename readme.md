# H2CS

Convert CPP Header `.h` to CSharp `.cs` binding

# Usage

1. Define a project name. Such as `AwesomeProject`. This is a very important part, many steps below are associated with
   this name.

2. After giving a name to the project, we also define an export (public) annotation of the apis.
    ```c
    // seen in H2CSVisitor constructor
    #define AWESOMEPROJECT_API
   
    // you can also define this macro as your export macro as well
    // https://github.com/AOMediaCodec/libavif/blob/397f74c8e289386eb7d309b2f8041d8a190db29a/include/avif/avif.h#L43
    #define AVIF_API AVIF_HELPER_EXPORT
    ```

3. Write a CPP header with all your public C function
    ```cpp
    extern "C" {
        // function name must start with your project name
        // function must be decorated with the macro defined in step 1        
        AWESOMEPROJECT_API int64_t AwesomeProjectGetVersion(int32_t &major, int32_t &minor, int32_t &patch);
        
        // you can define a type mapping temporary
        AWESOMEPROJECT_API int64_t AwesomeProjectFoo([[milize::CSharpType("void*")]] void *data);
   
        // you can also define the type mapping later
        AWESOMEPROJECT_API int64_t AwesomeProjectBar(asesome::project::someclass *t);
    }
    ```
   
4. Use H2CS to convert this CPP Header `.h` to CSharp `.cs` binding. \  
   In `settings.gradle.kts`
    ```kotlin
    pluginManagement {
        repositories {
            mavenLocal()
            mavenCentral()
            maven { url = uri("https://plugins.gradle.org/m2/") }
            maven { url = uri("https://maven.para.party") }
        }
    }
    
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
    
    rootProject.name = "AwesomeProject"
    ```
   In `build.gradle.kts`
    ```kotlin
    plugins {
        id("party.para.h2cs") version "1.0.1" apply true
    }
    
    group = "com.example.awesomeproject"
    version = "0.0.1"
    
    subprojects {
        group = "com.example.awesomeproject"
        version = "0.0.1"
        repositories { repo() }
    }
    
    repositories { repo() }
    
    tasks {
        h2cs {
            projectName = "AwesomeProject"

            sourceFilePath = "path to your header file defined in step 3" 
            csharpBindingOutputPath = "path to the .cs file you want to save"
    
            // use for Apple Framework cpp bridge
            cppFrameworkBindingOutputPath = "path to the .cpp file you want to save"
   
            // you can define your type mapping in H2CS configuration
            addTypeMapping(
                listOf("asesome::project::someclass") to "IntPtr",
            )
        }
    }
    
    fun RepositoryHandler.repo() {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://maven.para.party") }
    }
    ```
5. Use `gradlew h2cs` to generate your CSharp `.cs` binding.

# Example
1. [MorizeroDev/Milestro](https://github.com/MorizeroDev/Milestro)

/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.publish.ivy

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.test.fixtures.ivy.IvyDescriptor

class IvyPublishDescriptorIntegTest extends AbstractIntegrationSpec {

    def module = ivyRepo.module("org.gradle", "publish", "2")

    def setup() {
        settingsFile << """
            rootProject.name = "${module.module}"
        """

        buildFile << """
            apply plugin: 'java'
            apply plugin: 'ivy-publish'

            version = '${module.revision}'
            group = '${module.organisation}'

            publishing {
                repositories {
                    ivy { url "${ivyRepo.uri}" }
                }
                publications {
                    ivy(IvyPublication) {
                        from components.java
                    }
                }
            }
        """
    }

    def "can customise descriptor xml during publication"() {
        when:
        succeeds 'publish'

        then:
        ":jar" in executedTasks

        and:
        module.ivy.revision == "2"

        when:
        buildFile << """
            publishing {
                publications {
                    ivy {
                        descriptor {
                            withXml {
                                asNode().info[0].@revision = "3"
                            }
                        }
                    }
                }
            }
        """
        succeeds 'publish'


        then:
        ":jar" in skippedTasks

        and:
        // Note that the modified “coordinates” do not affect how the module is published
        // This is not the desired behaviour and will be fixed in the future so that XML modification changes the publication model consistently.
        module.ivy.revision == "3"
    }

    def "can publish with non-ascii characters"() {
        def organisation = 'group-√æず'
        def moduleName = 'artifact-∫ʙぴ'
        def version = 'version-₦ガき∆'
        def description = 'description-ç√∫'
        def nonAsciiModule = ivyRepo.module(organisation, moduleName, version)

        given:
        settingsFile.text = "rootProject.name = '${moduleName}'"
        buildFile.text = """
            apply plugin: 'ivy-publish'

            group = '${organisation}'
            version = '${version}'

            publishing {
                repositories {
                    ivy { url "${ivyRepo.uri}" }
                }
                publications {
                    ivy(IvyPublication) {
                        descriptor.withXml {
                            asNode().info[0].appendNode('description', "${description}")
                        }
                    }
                }
            }
        """
        when:
        succeeds 'publish'

        then:
        nonAsciiModule.assertPublished()
        nonAsciiModule.ivy.description == description
    }

    def "can generate ivy.xml without publishing"() {
        given:
        def moduleName = module.module
        buildFile << """
            generateIvyModuleDescriptor {
                destination = 'generated-ivy.xml'
            }
        """

        when:
        succeeds 'generateIvyModuleDescriptor'

        then:
        file('generated-ivy.xml').assertIsFile()
        IvyDescriptor ivy = new IvyDescriptor(file('generated-ivy.xml'))
        with (ivy.artifacts[moduleName]) {
            name == moduleName
            ext == 'jar'
            conf == ['runtime']
        }

        and:
        module.ivyFile.assertDoesNotExist()
    }

    def "produces sensible error when withXML fails"() {
        when:
        buildFile << """
            publishing {
                publications {
                    ivy {
                        descriptor.withXml {
                            asNode().foo = "3"
                        }
                    }
                }
            }
        """
        fails 'publish'

        then:
        failure.assertHasDescription("Execution failed for task ':generateIvyModuleDescriptor'")
        failure.assertHasCause("Could not apply withXml() to Ivy module descriptor")
        failure.assertHasCause("No such property: foo for class: groovy.util.Node")
    }
}

package com.aaqibhussain.admin

import org.json.JSONArray
import org.json.JSONObject

data class HeroMetric(
    val label: String,
    val value: String
)

data class HeroData(
    val greeting: String,
    val headline: String,
    val subheadline: String,
    val supportingLine: String,
    val ctaText1: String,
    val ctaText2: String,
    val metrics: List<HeroMetric>
)

data class AboutData(
    val paragraphs: List<String>,
    val quote: String,
    val location: String,
    val experienceYears: String,
    val focusAreas: List<String>
)

data class JourneyItem(
    val id: String,
    val year: String,
    val title: String,
    val subtitle: String,
    val story: String,
    val highlights: List<String>,
    val badge: String
)

data class SkillItem(
    val name: String,
    val level: String
)

data class SkillCategory(
    val id: String,
    val category: String,
    val iconName: String,
    val items: List<SkillItem>
)

data class CertSignatory(
    val name: String,
    val title: String
)

data class CertItem(
    val id: String,
    val title: String,
    val issuer: String,
    val issueDate: String,
    val expiryDate: String,
    val description: String,
    val badgeText: String,
    val verifyUrl: String,
    val signatories: List<CertSignatory>,
    val certImage: String
)

data class AttachmentItem(
    val id: String,
    val name: String,
    val mimeType: String,
    val url: String,
    val size: Long,
    val createdAt: String
)

data class WriteupItem(
    val id: String,
    val title: String,
    val targetApp: String,
    val severity: String,
    val status: String,
    val summary: String,
    val fullDescription: String,
    val stepsToReproduce: List<String>,
    val requestPayloads: List<String>,
    val impact: String,
    val recommendation: String,
    val tags: List<String>,
    val featured: Boolean,
    val order: Int,
    val dateDisclosed: String,
    val markdownContent: String
)

data class PlatformItem(
    val id: String,
    val platform: String,
    val username: String,
    val url: String,
    val icon: String,
    val badgeText: String,
    val description: String
)

data class SocialItem(
    val id: String,
    val platform: String,
    val username: String,
    val url: String
)

data class ContactMessage(
    val id: String,
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val createdAt: String,
    val read: Boolean
)

data class SiteData(
    val rawJson: JSONObject,
    val hero: HeroData,
    val about: AboutData,
    val journey: List<JourneyItem>,
    val skills: List<SkillCategory>,
    val certs: List<CertItem>,
    val writeups: List<WriteupItem>,
    val platforms: List<PlatformItem>,
    val socials: List<SocialItem>,
    val attachments: List<AttachmentItem>,
    val inbox: List<ContactMessage>,
    val updatedAt: String
)

object SiteDataSerializer {
    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).map { optString(it, "") }.filter { it.isNotEmpty() }
    }

    fun parse(json: JSONObject): SiteData {
        val heroObj = json.optJSONObject("hero") ?: JSONObject()
        val metricsArr = heroObj.optJSONArray("metrics") ?: JSONArray()
        val metrics = (0 until metricsArr.length()).mapNotNull { i ->
            val m = metricsArr.optJSONObject(i) ?: return@mapNotNull null
            HeroMetric(
                label = m.optString("label", ""),
                value = m.optString("value", "")
            )
        }
        val hero = HeroData(
            greeting = heroObj.optString("greeting", ""),
            headline = heroObj.optString("headline", ""),
            subheadline = heroObj.optString("subheadline", ""),
            supportingLine = heroObj.optString("supportingLine", ""),
            ctaText1 = heroObj.optString("ctaText1", ""),
            ctaText2 = heroObj.optString("ctaText2", ""),
            metrics = metrics
        )

        val aboutObj = json.optJSONObject("about") ?: JSONObject()
        val about = AboutData(
            paragraphs = aboutObj.optJSONArray("paragraphs").toStringList(),
            quote = aboutObj.optString("quote", ""),
            location = aboutObj.optString("location", ""),
            experienceYears = aboutObj.optString("experienceYears", ""),
            focusAreas = aboutObj.optJSONArray("focusAreas").toStringList()
        )

        val journeyArr = json.optJSONArray("journey") ?: JSONArray()
        val journey = (0 until journeyArr.length()).mapNotNull { i ->
            val j = journeyArr.optJSONObject(i) ?: return@mapNotNull null
            JourneyItem(
                id = j.optString("id", "j-$i"),
                year = j.optString("year", ""),
                title = j.optString("title", ""),
                subtitle = j.optString("subtitle", ""),
                story = j.optString("story", ""),
                highlights = j.optJSONArray("highlights").toStringList(),
                badge = j.optString("badge", "")
            )
        }

        val skillsArr = json.optJSONArray("skills") ?: JSONArray()
        val skills = (0 until skillsArr.length()).mapNotNull { i ->
            val s = skillsArr.optJSONObject(i) ?: return@mapNotNull null
            val itemsArr = s.optJSONArray("items") ?: JSONArray()
            val items = (0 until itemsArr.length()).mapNotNull { k ->
                val item = itemsArr.optJSONObject(k) ?: return@mapNotNull null
                SkillItem(
                    name = item.optString("name", ""),
                    level = item.optString("level", "Advanced")
                )
            }
            SkillCategory(
                id = s.optString("id", "cat-$i"),
                category = s.optString("category", ""),
                iconName = s.optString("iconName", "Shield"),
                items = items
            )
        }

        val certsArr = json.optJSONArray("certs") ?: JSONArray()
        val certs = (0 until certsArr.length()).mapNotNull { i ->
            val c = certsArr.optJSONObject(i) ?: return@mapNotNull null
            val sigArr = c.optJSONArray("signatories") ?: JSONArray()
            val sigs = (0 until sigArr.length()).mapNotNull { k ->
                val sg = sigArr.optJSONObject(k) ?: return@mapNotNull null
                CertSignatory(sg.optString("name", ""), sg.optString("title", ""))
            }
            CertItem(
                id = c.optString("id", "cert-$i"),
                title = c.optString("title", ""),
                issuer = c.optString("issuer", ""),
                issueDate = c.optString("issueDate", ""),
                expiryDate = c.optString("expiryDate", ""),
                description = c.optString("description", ""),
                badgeText = c.optString("badgeText", ""),
                verifyUrl = c.optString("verifyUrl", ""),
                signatories = sigs,
                certImage = c.optString("certImage", "")
            )
        }

        val writeupsArr = json.optJSONArray("writeups") ?: JSONArray()
        val writeups = (0 until writeupsArr.length()).mapNotNull { i ->
            val w = writeupsArr.optJSONObject(i) ?: return@mapNotNull null
            WriteupItem(
                id = w.optString("id", "wu-$i"),
                title = w.optString("title", ""),
                targetApp = w.optString("targetApp", ""),
                severity = w.optString("severity", "high"),
                status = w.optString("status", "Resolved"),
                summary = w.optString("summary", ""),
                fullDescription = w.optString("fullDescription", ""),
                stepsToReproduce = w.optJSONArray("stepsToReproduce").toStringList(),
                requestPayloads = w.optJSONArray("requestPayloads").toStringList(),
                impact = w.optString("impact", ""),
                recommendation = w.optString("recommendation", ""),
                tags = w.optJSONArray("tags").toStringList(),
                featured = w.optBoolean("featured", true),
                order = w.optInt("order", i + 1),
                dateDisclosed = w.optString("dateDisclosed", "Disclosed & Patched"),
                markdownContent = w.optString("markdownContent", "")
            )
        }

        val platformsArr = json.optJSONArray("platforms") ?: JSONArray()
        val platforms = (0 until platformsArr.length()).mapNotNull { i ->
            val p = platformsArr.optJSONObject(i) ?: return@mapNotNull null
            PlatformItem(
                id = p.optString("id", "plat-$i"),
                platform = p.optString("platform", ""),
                username = p.optString("username", ""),
                url = p.optString("url", ""),
                icon = p.optString("icon", ""),
                badgeText = p.optString("badgeText", ""),
                description = p.optString("description", "")
            )
        }

        val socialsArr = json.optJSONArray("socials") ?: JSONArray()
        val socials = (0 until socialsArr.length()).mapNotNull { i ->
            val sc = socialsArr.optJSONObject(i) ?: return@mapNotNull null
            SocialItem(
                id = sc.optString("id", "soc-$i"),
                platform = sc.optString("platform", ""),
                username = sc.optString("username", ""),
                url = sc.optString("url", "")
            )
        }

        val attachArr = json.optJSONArray("attachments") ?: JSONArray()
        val attachments = (0 until attachArr.length()).mapNotNull { i ->
            val a = attachArr.optJSONObject(i) ?: return@mapNotNull null
            AttachmentItem(
                id = a.optString("id", ""),
                name = a.optString("name", ""),
                mimeType = a.optString("mimeType", "image/png"),
                url = a.optString("url", ""),
                size = a.optLong("size", 0L),
                createdAt = a.optString("createdAt", "")
            )
        }

        val inboxArr = json.optJSONArray("inbox") ?: JSONArray()
        val inbox = (0 until inboxArr.length()).mapNotNull { i ->
            val m = inboxArr.optJSONObject(i) ?: return@mapNotNull null
            ContactMessage(
                id = m.optString("id", ""),
                name = m.optString("name", ""),
                email = m.optString("email", ""),
                subject = m.optString("subject", ""),
                message = m.optString("message", ""),
                createdAt = m.optString("createdAt", ""),
                read = m.optBoolean("read", false)
            )
        }

        return SiteData(
            rawJson = json,
            hero = hero,
            about = about,
            journey = journey,
            skills = skills,
            certs = certs,
            writeups = writeups,
            platforms = platforms,
            socials = socials,
            attachments = attachments,
            inbox = inbox,
            updatedAt = json.optString("updatedAt", "")
        )
    }

    fun toJson(data: SiteData): JSONObject {
        val root = JSONObject(data.rawJson.toString())

        val heroObj = JSONObject().apply {
            put("greeting", data.hero.greeting)
            put("headline", data.hero.headline)
            put("subheadline", data.hero.subheadline)
            put("supportingLine", data.hero.supportingLine)
            put("ctaText1", data.hero.ctaText1)
            put("ctaText2", data.hero.ctaText2)
            put("metrics", JSONArray().apply {
                data.hero.metrics.forEach { m ->
                    put(JSONObject().apply {
                        put("label", m.label)
                        put("value", m.value)
                    })
                }
            })
        }
        root.put("hero", heroObj)

        val aboutObj = JSONObject().apply {
            put("paragraphs", JSONArray(data.about.paragraphs))
            put("quote", data.about.quote)
            put("location", data.about.location)
            put("experienceYears", data.about.experienceYears)
            put("focusAreas", JSONArray(data.about.focusAreas))
        }
        root.put("about", aboutObj)

        root.put("journey", JSONArray().apply {
            data.journey.forEach { j ->
                put(JSONObject().apply {
                    put("id", j.id)
                    put("year", j.year)
                    put("title", j.title)
                    put("subtitle", j.subtitle)
                    put("story", j.story)
                    put("highlights", JSONArray(j.highlights))
                    put("badge", j.badge)
                })
            }
        })

        root.put("skills", JSONArray().apply {
            data.skills.forEach { cat ->
                put(JSONObject().apply {
                    put("id", cat.id)
                    put("category", cat.category)
                    put("iconName", cat.iconName)
                    put("items", JSONArray().apply {
                        cat.items.forEach { sk ->
                            put(JSONObject().apply {
                                put("name", sk.name)
                                put("level", sk.level)
                            })
                        }
                    })
                })
            }
        })

        root.put("certs", JSONArray().apply {
            data.certs.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id)
                    put("title", c.title)
                    put("issuer", c.issuer)
                    put("issueDate", c.issueDate)
                    put("expiryDate", c.expiryDate)
                    put("description", c.description)
                    put("badgeText", c.badgeText)
                    put("verifyUrl", c.verifyUrl)
                    put("certImage", c.certImage)
                    put("signatories", JSONArray().apply {
                        c.signatories.forEach { sg ->
                            put(JSONObject().apply {
                                put("name", sg.name)
                                put("title", sg.title)
                            })
                        }
                    })
                })
            }
        })

        root.put("writeups", JSONArray().apply {
            data.writeups.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id)
                    put("title", w.title)
                    put("targetApp", w.targetApp)
                    put("severity", w.severity)
                    put("status", w.status)
                    put("summary", w.summary)
                    put("fullDescription", w.fullDescription)
                    put("stepsToReproduce", JSONArray(w.stepsToReproduce))
                    put("requestPayloads", JSONArray(w.requestPayloads))
                    put("impact", w.impact)
                    put("recommendation", w.recommendation)
                    put("tags", JSONArray(w.tags))
                    put("featured", w.featured)
                    put("order", w.order)
                    put("dateDisclosed", w.dateDisclosed)
                    put("markdownContent", w.markdownContent)
                })
            }
        })

        root.put("platforms", JSONArray().apply {
            data.platforms.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("platform", p.platform)
                    put("username", p.username)
                    put("url", p.url)
                    put("icon", p.icon)
                    put("badgeText", p.badgeText)
                    put("description", p.description)
                })
            }
        })

        root.put("socials", JSONArray().apply {
            data.socials.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("platform", s.platform)
                    put("username", s.username)
                    put("url", s.url)
                })
            }
        })

        root.put("inbox", JSONArray().apply {
            data.inbox.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("email", m.email)
                    put("subject", m.subject)
                    put("message", m.message)
                    put("createdAt", m.createdAt)
                    put("read", m.read)
                })
            }
        })

        return root
    }
}

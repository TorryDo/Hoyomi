package com.torrydo.hoyomi.model.feedPackage

data class DataX(
//    val all_awardings: List<AllAwarding>,
//    val allow_live_comments: Boolean,
//    val approved_at_utc: Any,
//    val approved_by: Any,
//    val archived: Boolean,
    val author: String,           /**  etc : staychillbruh-_- */
//    val author_flair_background_color: Any,
//    val author_flair_css_class: Any,
//    val author_flair_richtext: List<Any>,
//    val author_flair_template_id: Any,
//    val author_flair_text: Any,
//    val author_flair_text_color: Any,
//    val author_flair_type: String,
    val author_fullname: String,         /** "t2_11ui86" */
//    val author_patreon_flair: Boolean,
//    val author_premium: Boolean,
//    val awarders: List<Any>,
//    val banned_at_utc: Any,
//    val banned_by: Any,
//    val can_gild: Boolean,
//    val can_mod_post: Boolean,
//    val category: Any,
//    val clicked: Boolean,
//    val content_categories: Any,
//    val contest_mode: Boolean,
//    val created: Double,
//    val created_utc: Double,
//    val discussion_type: Any,
//    val distinguished: String,
//    val domain: String,
//    val downs: Int,
/**    val edited: Boolean ,    Log.e/ : expected boolean but was number     */
//    val gilded: Int,
//    val gildings: Gildings,
//    val hidden: Boolean,
//    val hide_score: Boolean,
    val id: String,
//    val is_crosspostable: Boolean,
//    val is_meta: Boolean,
//    val is_original_content: Boolean,
//    val is_reddit_media_domain: Boolean,
//    val is_robot_indexable: Boolean,
//    val is_self: Boolean,
    val is_video: Boolean,
//    val likes: Any,
//    val link_flair_background_color: String,
//    val link_flair_css_class: Any,
//    val link_flair_richtext: List<Any>,
//    val link_flair_template_id: String,
//    val link_flair_text: Any,
//    val link_flair_text_color: String,
//    val link_flair_type: String,
//    val locked: Boolean,
    val media: Any,
    val media_embed: MediaEmbed,
    val media_only: Boolean,
//    val mod_note: Any,
//    val mod_reason_by: Any,
//    val mod_reason_title: Any,
//    val mod_reports: List<Any>,
    val name: String,         /** etc : t3_ljgk8p */
//    val no_follow: Boolean,
//    val num_comments: Int,
//    val num_crossposts: Int,
//    val num_reports: Any,
    val over_18: Boolean,
//    val parent_whitelist_status: String,
    val permalink: String,
    val pinned: Boolean,
//    val post_hint: String,
    val preview: Preview,
//    val pwls: Int,
//    val quarantine: Boolean,
//    val removal_reason: Any,
//    val removed_by: Any,
//    val removed_by_category: Any,
//    val report_reasons: Any,
//    val saved: Boolean,
//    val score: Int,
//    val secure_media: Any,
//    val secure_media_embed: SecureMediaEmbed,
//    val selftext: String,
//    val selftext_html: String,
//    val send_replies: Boolean,
//    val spoiler: Boolean,
//    val stickied: Boolean,
    val subreddit: String,        /** anime wallpaper */
    val subreddit_id: String,
    val subreddit_name_prefixed: String,    /**  r/Animewallpaper */
    val subreddit_subscribers: Int,           /** etc : 134221 */
//    val subreddit_type: String,
//    val suggested_sort: String,
    val thumbnail: String,
    val thumbnail_height: Any,
    val thumbnail_width: Any,
    val title: String,
//    val top_awarded_type: Any,
//    val total_awards_received: Int,
//    val treatment_tags: List<Any>,
    val ups: Int,                      /** etc : 1243*/
    val upvote_ratio: Double,          /** etc : 1.0 */
    val url: String,
    val url_overridden_by_dest: String,
//    val user_reports: List<Any>,
//    val view_count: Any,
    val visited: Boolean,
//    val whitelist_status: String,
//    val wls: Int
)
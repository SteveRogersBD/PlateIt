package com.example.plateit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RecipeBlogs {

    public SearchMetadata search_metadata;
    public SearchParameters search_parameters;
    public SearchInformation search_information;
    public ArrayList<RecipesResult> recipes_results;
    public ArrayList<OrganicResult> organic_results;
    public ArrayList<RelatedSearch> related_searches;
    public ArrayList<DiscussionsAndForum> discussions_and_forums;
    public Pagination pagination;
    public SerpapiPagination serpapi_pagination;

    public class Answer {
        public String snippet;
        public String link;
        public ArrayList<String> extensions;
    }

    public class DiscussionsAndForum {
        public String title;
        public String link;
        public String date;
        public ArrayList<String> extensions;
        public String source;
        public ArrayList<Answer> answers;
    }

    public class OrganicResult {
        public int position;
        public String title;
        public String link;
        public String redirect_link;
        public String displayed_link;
        public String favicon;
        public String date;
        public String snippet;
        public ArrayList<String> snippet_highlighted_words;
        public String source;
        public String thumbnail;
    }

    public class OtherPages {
        @JsonProperty("2")
        public String _2;
        @JsonProperty("3")
        public String _3;
        @JsonProperty("4")
        public String _4;
        @JsonProperty("5")
        public String _5;
        @JsonProperty("6")
        public String _6;
        @JsonProperty("7")
        public String _7;
        @JsonProperty("8")
        public String _8;
        @JsonProperty("9")
        public String _9;
        @JsonProperty("10")
        public String _10;
    }

    public class Pagination {
        public int current;
        public String next;
        public OtherPages other_pages;
    }

    public class RecipesResult {
        public String title;
        public String link;
        public String source;
        public double rating;
        public int reviews;
        public String total_time;
        public ArrayList<String> ingredients;
        public String thumbnail;
    }

    public class RelatedSearch {
        public int block_position;
        public String query;
        public String link;
        public String serpapi_link;
    }

    public class SearchInformation {
        public String query_displayed;
        public long total_results;
        public double time_taken_displayed;
        public String organic_results_state;
    }

    public class SearchMetadata {
        public String id;
        public String status;
        public String json_endpoint;
        public String pixel_position_endpoint;
        public String created_at;
        public String processed_at;
        public String google_url;
        public String raw_html_file;
        public double total_time_taken;
    }

    public class SearchParameters {
        public String engine;
        public String q;
        public String google_domain;
        public String num;
        public String device;
    }

    public class SerpapiPagination {
        public int current;
        public String next_link;
        public String next;
        public OtherPages other_pages;
    }

}

package com.thanos.participant1.story;

import java.util.List;

public record DialogueView(String speaker, String text, List<String> choices, int selected) { }

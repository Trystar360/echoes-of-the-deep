/*
 * Inspection log templates — data driven.
 * Add a new object to TEMPLATES to create a brand-new daily log type;
 * the whole UI (forms, history, print) is generated from this shape.
 *
 *   template = {
 *     id, name, subtitle, glyph, accent,
 *     sections: [ { title, items: [ { id, label, hint? } ] } ]
 *   }
 *
 * Every log captures shared meta (date, group, participants, inspector)
 * plus a Pass / Fail / N/A status + initials + note for each item.
 */

const TEMPLATES = [
  {
    id: "skytrail",
    name: "SkyTrail Daily Checklist",
    subtitle: "High ropes course · open / run / close",
    glyph: "⛰",
    accent: "#5fd0c4",
    sections: [
      {
        title: "Pre-Use Inspection",
        items: [
          { id: "harnesses", label: "Harnesses", hint: "Fraying at seams, rips, tears, hardware intact" },
          { id: "slinglines", label: "Slinglines", hint: "Wear, sheath damage, secure connections" },
          { id: "etks", label: "ETK's", hint: "Emergency tool kits present and complete" },
          { id: "steel-structure", label: "Steel Structure", hint: "No cracks, deformation, or corrosion" },
          { id: "nuts-bolts", label: "Nuts & Bolts", hint: "Tight, no missing fasteners" },
          { id: "track", label: "Track", hint: "Clear, smooth, trolleys move freely" },
          { id: "platforms", label: "Platforms", hint: "Secure, no trip hazards, edges sound" },
          { id: "surrounding-area", label: "Surrounding Area", hint: "Clear of debris, obstacles, hazards" },
          { id: "cross-elements", label: "Cross each element on the course", hint: "Inspect every element as you go across" },
        ],
      },
      {
        title: "Activity Set Up",
        items: [
          { id: "open-gate", label: "Open Gate" },
          { id: "set-out-gear", label: "Set out Harnesses and Slinglines" },
          { id: "etk-setup", label: "ETK Inspected and set up (pillars 3, 5)" },
        ],
      },
      {
        title: "Activity Closing",
        items: [
          { id: "etk-stored", label: "ETK taken down and stored" },
          { id: "gates-locked", label: "Gates closed / locked" },
          { id: "gear-stored", label: "Store all harnesses / slinglines" },
          { id: "equipment-returned", label: "Return all equipment", hint: "Whistle, finger key, radio holster, scissors" },
          { id: "lock-3rd-floor", label: "LOCK DOOR GOING TO 3rd FLOOR!", hint: "Critical — do not skip" },
        ],
      },
    ],
  },
  {
    id: "power-swing",
    name: "Daily Indoor Power Swing Inspection Log",
    subtitle: "Pre-flight checks before the first rider",
    glyph: "🪂",
    accent: "#f0a35e",
    sections: [
      {
        title: "Items to Inspect",
        items: [
          { id: "harnesses", label: "Harnesses", hint: "Fraying at seams, rips or tears" },
          { id: "helmets", label: "Helmets", hint: "Dials and clips work properly, no cracks" },
          {
            id: "lanyard-attachment",
            label: "Lanyard & attachment rapid links and carabiner",
            hint: "Gate closes completely with adequate rebound; no nicks that could cause rope abrasion",
          },
          { id: "pull-rope", label: "Pull rope", hint: "Knots at either end, freely flows through pulleys" },
          { id: "pulleys-cables", label: "Pulleys and cables", hint: "Visually check from the ground" },
          { id: "release-clip", label: "Release clip", hint: "Releases properly, does not stick, release rope not frayed" },
          { id: "test-ride", label: "Test Ride", hint: "Weight the lanyard before the first participant" },
        ],
      },
    ],
  },
];

const TEMPLATE_BY_ID = Object.fromEntries(TEMPLATES.map((t) => [t.id, t]));

/** Inline popup. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document;

$.plugin2('popup', {
    '_defaultOptions': {
        'padding': {
            'left': 35,
            'right': 35,
            'top': 20
        }
    },
    
    '_create': function(element, options) {
        var $inner = $(element);
        var $container = $('<div/>', { 'class': 'popup' });
        var $content = $('<div/>', { 'class': 'content' });
        var $closeButton = $('<div/>', { 'class': 'closeButton' });

        var name = $inner.attr('name');
        if (name) {
            $container.attr('name', name);
        }

        // Bind open and close events.
        $container.bind('open.popup', function() {
            var $original = $(this);
            var scrollLeft = $original.data('popup-scrollLeft');
            var scrollTop = $original.data('popup-scrollTop');
            if (typeof scrollLeft !== 'number' && typeof scrollTop !== 'number') {
                $original.data('popup-scrollLeft', $win.scrollLeft());
                $original.data('popup-scrollTop', $win.scrollTop());
            }
            $original.show();
        });

        $container.bind('restoreOriginalPosition.popup', function() {
            var $original = $(this);
            var scrollLeft = $original.data('popup-scrollLeft');
            var scrollTop = $original.data('popup-scrollTop');
            $original.removeData('popup-scrollLeft');
            $original.removeData('popup-scrollTop');
            if (typeof scrollLeft === 'number' && typeof scrollTop === 'number') {
                $win.scrollLeft(scrollLeft);
                $win.scrollTop(scrollTop);
            }
        });

        $container.bind('close.popup', function() {
            var $original = $(this);
            $original.hide();
            $('.popup').each(function() {
                var $popup = $(this);
                var $source = $popup.popup('source');
                if ($source && $.contains($original[0], $source[0])) {
                    $popup.popup('close');
                }
            });
        });

        $closeButton.bind('click.popup', function() {
            $(this).popup('close');
        });

        var $body = $(doc.body);
        $content.append($inner);
        $content.append($closeButton);
        $container.append($content);
        $body.append($container);
    },

    // Opens the popup.
    'open': function() {
        this.$caller.popup('container').trigger('open');
        return this.$caller;
    },

    'restoreOriginalPosition': function() {
        this.$caller.popup('container').trigger('restoreOriginalPosition');
        return this.$caller;
    },

    // Closes the popup.
    'close': function() {
        this.container().trigger('close');
        return this.$caller;
    },

    // Returns the enclosing element that contains the popup.
    'container': function() {
        return this.$caller.closest('.popup');
    },

    // Returns the popup content element.
    'content': function() {
        return this.$caller.popup('container').find('> .content');
    },

    // Returns the source element that triggered the popup to open.
    'source': function($newSource) {
        var options = this.option();

        if (typeof $newSource === 'undefined') {
            var container = this.$caller.popup('container')[0];
            return container ? $.data(container, 'popup-$source') : null;

        } else {
            var $container = this.$caller.popup('container');
            $container.each(function() {
                $.data(this, 'popup-$source', $newSource);
            });

            // Change position.
            var sourceOffset = $newSource.offset();
            var popupWidth = $container.outerWidth();

            // Make sure left is within bounds.
            var markerDelta = 0;
            var left = sourceOffset.left + ($newSource.outerWidth() - popupWidth) / 2;
            if (left < options.padding.left) {
                markerDelta = left - options.padding.left;
                left = options.padding.left;
            } else {
                var leftDelta = left + popupWidth - $(window).width() + options.padding.right;
                if (leftDelta > 0) {
                    markerDelta = leftDelta;
                    left -= leftDelta;
                }
            }

            // Create a arrow-like marker.
            var $content = $container.popup('content');
            var $marker = $content.find('> .marker');
            if ($marker.length === 0) {
                $marker = $('<div/>', { 'class': 'marker' });
                $content.append($marker);
            }
            var markerLeft = (popupWidth  - $marker.outerWidth()) / 2 + markerDelta;
            $marker.css('left', markerLeft < 5 ? 5 : markerLeft);

            // Make sure top is within bounds.
            var top = sourceOffset.top + $newSource.outerHeight() / 2;
            if (top < 30) {
                top = 30;
            }

            // Adjust left/top if position is fixed.
            var $newSourceParent = $newSource.offsetParent();
            var isFixed = $newSourceParent.css('position') == 'fixed';
            if (isFixed) {
                left -= $(window).scrollLeft();
                top -= $(window).scrollTop();
            }

            $container.css({
                'left': left,
                'margin': 0,
                'position': isFixed ? 'fixed' : 'absolute',
                'top': top,
                'z-index': $newSourceParent.zIndex() + 1
            });

            return this.$caller;
        }
    }
});

// Clicking outside the popups should close them all.
$win.click(function(event) {
    var target = event.target;
    if ($(target).popup('container').length === 0) {
        $('.popup').each(function() {
            var $container = $(this);

            // Suppress the close if the click was within what triggered the popup.
            var $source = $container.popup('source');
            if ($source && $source.length > 0) {
                var source = $source[0];
                if (!(source == target || $.contains(source, target))) {
                    $container.popup('close');
                }
            }
        });
    }
});

// Hitting ESC should close all popups too.
$win.keydown(function(event) {
    if (event.which === 27) {
        var $containers = $('.popup');
        if ($containers.length > 0) {
            $containers.popup('close');
            return false;
        } else {
            return true;
        }
    }
});

}(jQuery, window));
